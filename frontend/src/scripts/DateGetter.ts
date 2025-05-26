class DateGetter {
    public static getLocalTime(date: Date) {
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return `${hours}:${minutes}`
    }

    public static getLocalDate(date: Date) {
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        return `${day}/${month}/${year}`;
    }

    public static getDifferenceInDates(date1: Date, date2: Date) {
        // в минутах
        const diffMs = date2.getTime() - date1.getTime();
        return Math.floor(diffMs / (1000 * 60))
    }
}

export default DateGetter