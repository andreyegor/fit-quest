class DateGetter {
    private static translations = {
        0: ["Январь", "Января"],
        1: ["Февраль", "Февраля"],
        2: ["Март", "Марта"],
        3: ["Апрель", "Апреля"],
        4: ["Май", "Мая"],
        5: ["Июнь", "Июня"],
        6: ["Июль", "Июля"],
        7: ["Август", "Августа"],
        8: ["Сентябрь", "Сентября"],
        9: ["Октябрь", "Октября"],
        10: ["Ноябрь", "Ноября"],
        11: ["Декабрь", "Декабря"],
    }

    public static translateMonth(date: Date, isGenitiveCase: boolean): string;
    public static translateMonth(month: number, isGenitiveCase: boolean): string;
    public static translateMonth(arg: Date | number, isGenitiveCase: boolean): string {
        let month = arg instanceof Date ? arg.getMonth() : arg;
        while (month < 0) {
            month += 12
        }
        return this.translations[month as keyof typeof this.translations][isGenitiveCase ? 1 : 0];
    }

    public static translatePeriod(date1: Date, date2: Date, start?: string): string {
        let textContent = start ?? ""
        if (this.isFullMonth(date1, date2)) {
            const isCurrentYear = new Date().getFullYear() === date1.getFullYear()
            textContent += `${this.translateMonth(date1, false)}${isCurrentYear ? "" : ` ${date1.getFullYear()}`}`
            return `${textContent}`
        } else {
            const period: string = `${this.getLocalDate(date1)} - ${this.getLocalDate(date2)}`
            return `${textContent}${textContent ? "<br>" : ""}${period}`
        }
    }

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

    public static isSameDay(date1: Date, date2: Date): boolean {
        return date1.getFullYear() === date2.getFullYear() &&
               date1.getMonth() === date2.getMonth() &&
               date1.getDate() === date2.getDate()
    }

    public static getDifferenceInDates(date1: Date, date2: Date) {
        // в минутах
        const diffMs = date2.getTime() - date1.getTime();
        return Math.floor(diffMs / (1000 * 60))
    }

    public static subtractMonths(date: Date, amount: number) {
        const newDate = new Date(date)
        const tempDate = new Date(date)
        tempDate.setDate(1)
        tempDate.setMonth(date.getMonth() - amount)

        if (this.getAmountOfDays(tempDate) < this.getAmountOfDays(date) && date.getDate() > this.getAmountOfDays(tempDate)) {
            newDate.setMonth(date.getMonth() - amount + 1, 0)
        } else {
            newDate.setMonth(date.getMonth() - amount)
        }
        return newDate
    }

    public static isFullMonth(date1: Date, date2: Date): boolean {
        return date1.getDate() === 1 && this.isLastDayOfMonth(date2) && date1.getMonth() === date2.getMonth() && date1.getFullYear() === date2.getFullYear()
    }

    public static getAmountOfDays(date: Date) {
        const month: number = date.getMonth()

        let amountOfDays = 31
        if ([3, 5, 8, 10].includes(month)) {
            amountOfDays = 30
        } else if (month === 1) {
            amountOfDays = date.getFullYear() % 4 === 0 ? 29 : 28
        }
        return amountOfDays
    }

    public static getYears(yearFrom: number, yearTo: number): number[] {
        const years = []
        for (let i = yearFrom; i <= yearTo; i++) {
            years.push(i)
        } return years
    }

    private static isLastDayOfMonth(date: Date): boolean {
        if (date.getMonth() === 1) {
            if (date.getFullYear() % 4 === 0) {
                return date.getDate() === 29
            } return date.getDate() === 28
        }

        if ([0, 2, 4, 6, 7, 9, 11].includes(date.getMonth())) {
            return date.getDate() === 31
        } return date.getDate() === 30
    }
}

export default DateGetter