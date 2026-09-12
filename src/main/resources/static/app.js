document.addEventListener('submit', function (event) {
    var form = event.target;
    var otazka = form.dataset.potvrdenie;
    if (!otazka || form.dataset.potvrdene === 'ano') {
        return;
    }
    var dialog = document.getElementById('potvrdenie');
    if (!dialog || typeof dialog.showModal !== 'function') {
        event.preventDefault();
        if (window.confirm(otazka)) {
            form.dataset.potvrdene = 'ano';
            form.submit();
        }
        return;
    }
    event.preventDefault();
    document.getElementById('potvrdenie-text').textContent = otazka;
    dialog.addEventListener('close', function () {
        if (dialog.returnValue === 'potvrdit') {
            form.dataset.potvrdene = 'ano';
            form.requestSubmit();
        }
    }, { once: true });
    dialog.showModal();
});

document.querySelectorAll('[data-zavriet]').forEach(function (button) {
    button.addEventListener('click', function () {
        var alert = button.closest('.alert');
        if (alert) {
            alert.remove();
        }
    });
});
