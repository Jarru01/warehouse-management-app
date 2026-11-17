#!/bin/sh
# Zalohy databazy PostgreSQL pre Warehouse App.
#
#   zaloha planovac      kazdy den o BACKUP_TIME (UTC) vytvori zalohu (predvolene)
#   zaloha zaloha        vytvori zalohu hned
#   zaloha zoznam        vypise existujuce zalohy
#   zaloha obnov SUBOR   obnovi databazu zo zalohy (cesta absolutna alebo relativna k /backups)
#
# Zalohy su vo formate pg_dump "custom" v /backups/daily, kopie z nedele vo weekly
# a z prveho dna mesiaca v monthly. Starsie nez BACKUP_KEEP_* sa mazu. Ak je nastavene
# BACKUP_OFFSITE (rclone ciel, napr. offsite:bucket/sklad), zalohy sa po vytvoreni
# skopiruju mimo servera. Ak je nastavene BACKUP_HEARTBEAT_URL, po uspesnej zalohe sa
# zavola (napr. healthchecks.io alebo push monitor v Uptime Kuma).
set -eu

ADRESAR="${BACKUP_DIR:-/backups}"
CAS="${BACKUP_TIME:-02:30}"
DNI="${BACKUP_KEEP_DAILY:-7}"
TYZDNE="${BACKUP_KEEP_WEEKLY:-4}"
MESIACE="${BACKUP_KEEP_MONTHLY:-6}"

log() {
    echo "$(date -u '+%Y-%m-%dT%H:%M:%SZ') zaloha: $*"
}

over_pripojenie() {
    : "${PGHOST:?PGHOST nie je nastavene}" "${PGDATABASE:?PGDATABASE nie je nastavene}"
    : "${PGUSER:?PGUSER nie je nastavene}" "${PGPASSWORD:?PGPASSWORD nie je nastavene}"
}

vycisti() {
    # $1 = adresar, $2 = kolko najnovsich zaloh ponechat
    ls -1t "$1"/*.dump 2>/dev/null | tail -n +"$(( $2 + 1 ))" | while read -r subor; do
        rm -f "$subor"
        log "zmazana stara zaloha $subor"
    done
}

zaloha() {
    mkdir -p "$ADRESAR/daily" "$ADRESAR/weekly" "$ADRESAR/monthly" || return 1
    nazov="sklad-$(date -u '+%Y%m%d-%H%M%S').dump"
    docasny="$ADRESAR/daily/.$nazov.tmp"

    if ! pg_dump --format=custom --no-owner --file="$docasny"; then
        rm -f "$docasny"
        log "CHYBA: pg_dump zlyhal"
        return 1
    fi
    if ! pg_restore --list "$docasny" > /dev/null; then
        rm -f "$docasny"
        log "CHYBA: zaloha sa neda precitat"
        return 1
    fi
    mv "$docasny" "$ADRESAR/daily/$nazov" || return 1
    log "vytvorena daily/$nazov ($(du -h "$ADRESAR/daily/$nazov" | cut -f1))"

    if [ "$(date -u +%u)" = "7" ]; then
        cp "$ADRESAR/daily/$nazov" "$ADRESAR/weekly/$nazov" && log "tyzdenna kopia weekly/$nazov"
    fi
    if [ "$(date -u +%d)" = "01" ]; then
        cp "$ADRESAR/daily/$nazov" "$ADRESAR/monthly/$nazov" && log "mesacna kopia monthly/$nazov"
    fi

    vycisti "$ADRESAR/daily" "$DNI"
    vycisti "$ADRESAR/weekly" "$TYZDNE"
    vycisti "$ADRESAR/monthly" "$MESIACE"

    if [ -n "${BACKUP_OFFSITE:-}" ]; then
        if rclone copy "$ADRESAR" "$BACKUP_OFFSITE" --exclude ".*" --log-level ERROR; then
            log "skopirovane mimo servera do $BACKUP_OFFSITE"
        else
            log "CHYBA: kopia mimo servera zlyhala"
            return 1
        fi
    fi

    if [ -n "${BACKUP_HEARTBEAT_URL:-}" ]; then
        wget -q -O /dev/null "$BACKUP_HEARTBEAT_URL" || log "upozornenie: heartbeat sa nepodarilo odoslat"
    fi
}

planovac() {
    log "planovac spusteny: zaloha kazdy den o $CAS UTC do $ADRESAR"
    while true; do
        teraz=$(date -u +%s)
        ciel=$(date -u -d "$(date -u +%Y-%m-%d) $CAS" +%s)
        if [ "$ciel" -le "$teraz" ]; then
            ciel=$((ciel + 86400))
        fi
        sleep "$((ciel - teraz))"
        zaloha || log "CHYBA: zaloha zlyhala, dalsi pokus o 24 hodin"
    done
}

zoznam() {
    for typ in daily weekly monthly; do
        echo "== $typ"
        ls -1hs "$ADRESAR/$typ" 2>/dev/null | grep '\.dump$' || echo "   (ziadne)"
    done
}

obnov() {
    subor="${1:?Zadaj subor zalohy, napr. daily/sklad-20261117-023000.dump}"
    if [ ! -f "$subor" ]; then
        subor="$ADRESAR/$subor"
    fi
    if [ ! -f "$subor" ]; then
        log "CHYBA: subor $1 neexistuje"
        exit 1
    fi
    ciel="${OBNOVA_DB:-$PGDATABASE}"
    if ! psql --dbname=postgres -tAc "select 1 from pg_database where datname = '$ciel'" | grep -q 1; then
        createdb "$ciel"
        log "vytvorena databaza $ciel"
    fi
    log "obnovujem $subor do databazy $ciel"
    pg_restore --clean --if-exists --no-owner --single-transaction --exit-on-error --dbname="$ciel" "$subor"
    log "obnova dokoncena"
}

if [ "$(id -u)" = "0" ]; then
    mkdir -p "$ADRESAR"
    chown -R postgres:postgres "$ADRESAR"
    exec gosu postgres "$0" "$@"
fi

over_pripojenie
case "${1:-planovac}" in
    planovac) planovac ;;
    zaloha) zaloha ;;
    zoznam) zoznam ;;
    obnov) obnov "${2:-}" ;;
    *) echo "Pouzitie: zaloha [planovac|zaloha|zoznam|obnov SUBOR]" >&2; exit 2 ;;
esac
