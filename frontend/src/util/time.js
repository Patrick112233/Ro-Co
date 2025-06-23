/**
 * Util function that converes a ISO 8601 timestamp UTC to a readable relative time distance to the current guest system timestamp.
 * @param createdAt {string} ISO 8601 timestamp UTC
 * @returns {string} strings like 1 min, 2 hours or 1 month evenen years
 */
function formatTimespan(createdAt) {

    const createdDate = new Date(createdAt);

    const now =  Date.now();
    /*const delatTimeSkew = 1001; // 1 second skew

    Cuases sporadic errors
    if (
        isNaN(createdDate.getTime()) ||
        createdDate.getFullYear() <= 2020 || // before development started
        now - createdDate.getTime() < -delatTimeSkew  // in the future is invalid
    ) {
        throw new Error("Invalid date format: " + createdAt);
    }
    const effectiveNow = createdDate > now ? createdDate : now;*/
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
        return `${months} ${months === 1 ? 'month' : 'months'}`;
    } else {
        const years = Math.floor(diffInMinutes / 525600);
        return `${years} ${years === 1 ? 'year' : 'years'}`;
    }
}

export default formatTimespan;