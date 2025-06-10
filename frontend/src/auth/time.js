
function formatTimespan(createdAt) {
    const now = new Date();
    const createdDate = new Date(createdAt);
    const diffInMinutes = Math.floor((now - createdDate) / (1000 * 60)); // Minuten direkt berechnen

    if (diffInMinutes < 60) {
        return `${diffInMinutes} ${diffInMinutes === 1 ? 'minute' : 'minutes'}`;
    } else if (diffInMinutes < 1440) {
        const hours = Math.floor(diffInMinutes / 60);
        return `${hours} ${hours === 1 ? 'hour' : 'hours'}`;
    } else if (diffInMinutes < 43200) {
        const days = Math.floor(diffInMinutes / 1440);
        return `${days} ${days === 1 ? 'day' : 'days'}`;
    } else if (diffInMinutes < 525600) {
        const months = Math.floor(diffInMinutes / 43200);
        return `${months} ${months === 1 ? 'month' : 'montsh'}`;
    } else {
        const years = Math.floor(diffInMinutes / 525600);
        return `${years} ${years === 1 ? 'year' : 'years'}`;
    }
}

export default formatTimespan;