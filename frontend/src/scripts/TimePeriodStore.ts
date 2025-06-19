class TimePeriodStore {
    private _timePeriod: [Date, Date]
    private subscribers: Array<(period: [Date, Date]) => void> = []

    constructor(initialPeriod: [Date, Date]) {
        this._timePeriod = initialPeriod
    }

    get period(): [Date, Date] {
        return [...this._timePeriod] as [Date, Date]
    }

    set period(value: [Date, Date]) {
        this._timePeriod = [...value]
        this.notifySubscribers()
    }

    subscribe(callback: (period: [Date, Date]) => void): void {
        this.subscribers.push(callback)
    }

    private notifySubscribers() {
        this.subscribers.forEach(callback => callback(this._timePeriod))
    }
}

export default TimePeriodStore