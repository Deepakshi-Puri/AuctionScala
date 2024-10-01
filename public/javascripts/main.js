function formatDateTime(dateTimeStr) {
    const dateTime = new Date(dateTimeStr);

    const date = dateTime.toLocaleDateString(undefined, {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });

    const time = dateTime.toLocaleTimeString(undefined, {
        hour: '2-digit',
        minute: '2-digit'
    });

    return `${date} at ${time}`;
}

function timer(tag, bidEnd) {
    const countDownDate = new Date(bidEnd).getTime();
    const now = new Date().getTime();
    const timeLeft = countDownDate - now;

    if (timeLeft <= 0) {
        location.reload();
    }

    const days = Math.floor(timeLeft / (1000 * 60 * 60 * 24));
    const hours = Math.floor((timeLeft % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((timeLeft % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((timeLeft % (1000 * 60)) / 1000);
    document.getElementById(tag).innerHTML = days + "d " + hours + "h " + minutes + "m " + seconds + "s ";
}