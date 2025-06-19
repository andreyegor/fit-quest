import DateGetter from "./DateGetter.ts";
import TimePeriodStore from "./TimePeriodStore.ts";

class CalendarButton {
    private selectors = {
        root: ".calendar-button",
        calendarLeftArrow: "#calendarButtonLeftArrow",
        calendarRightArrow: "#calendarButtonRightArrow",
        calendarTimePeriod: ".calendar-button__time-period",
        datePicker: ".date-picker",
        datePickerYears: ".date-picker--years",
        datePickerLeftArrow: "#datePickerLeftArrow",
        datePickerRightArrow: "#datePickerRightArrow",
        datePickerList: ".date-picker__list",
        datePickerItem: ".date-picker__item",
        datePickerButton: ".date-picker__button",
        month: ".date-picker__month",
        year: ".date-picker__year",
    }
    private stateSelectors = {
        isActive: ".is-active",
        disabled: ".disabled"
    }
    private rootElement: HTMLElement
    private store: TimePeriodStore
    private showingPeriod: Date
    private potentialTimePeriod: Date[] = []

    constructor(store: TimePeriodStore) {
        const rootElement = document.querySelector(this.selectors.root) as HTMLElement | null
        if (!rootElement) {
            throw new Error(`CalendarButton (constructor): calendarButtonElement not found`)
        }
        this.rootElement = rootElement
        this.store = store
        store.subscribe(() => {
            this.render(this.isYears())
        })
        this.showingPeriod = new Date(this.store.period[1])
        this.bindEvents()
        this.render(false)
    }

    private bindEvents(): void {
        document.addEventListener("click", event => this.onClick(event))
        this.rootElement.addEventListener("mouseover", event => this.onOver(event))

        const pickerListElement = this.rootElement.querySelector(this.selectors.datePickerList) as HTMLElement | null
        if (!pickerListElement) {
            console.error("CalendarButton.ts (bindEvents): pickerListElement not found")
            return
        }
        pickerListElement.addEventListener("mouseleave", event => this.onLeave(event))
    }

    private onClick(event: MouseEvent): void {
        const target = event.target as HTMLElement | null
        if (!target) {
            return
        }

        if (target.matches(this.selectors.calendarTimePeriod)) {
            const datePickerElement = this.rootElement.querySelector(this.selectors.datePicker)
            if (datePickerElement) {
                this.togglePicker(!datePickerElement.matches(this.stateSelectors.isActive))
                return
            }
        } else if (!target.closest(this.selectors.datePicker)) {
            this.togglePicker(false)
        }

        if (target.matches(this.selectors.datePickerButton) && !target.classList.contains(this.stateSelectors.disabled.slice(1))) {
            this.pickTrainingsPeriod()
            this.togglePicker(false)
            // this.render(false)
            return
        } else if (target.matches(this.selectors.year + this.stateSelectors.isActive)) {
            this.render(true)
        } else if (target.closest(this.selectors.datePickerLeftArrow)) {
            this.togglePickerMonth(false)
            return
        } else if (target.closest(this.selectors.datePickerRightArrow)) {
            this.togglePickerMonth(true)
            return
        } else if (target.closest(this.selectors.calendarLeftArrow)) {
            this.toggleRootMonth(false)
            return
        } else if (target.closest(this.selectors.calendarRightArrow)) {
            this.toggleRootMonth(true)
            return
        } else if (target.matches(this.selectors.datePickerItem)) {
            if (this.isYears()) {
                const year = Number(target.dataset.date)
                if (year) {
                    this.togglePickerYear(year)
                }
            } else {
                const day = Number(target.dataset.date)
                if (day) {
                    if (this.potentialTimePeriod.length === 1) {
                        this.potentialTimePeriod.push(
                            new Date(this.showingPeriod.getFullYear(), this.showingPeriod.getMonth(), day, 23, 59, 59, 999)
                        )
                        this.sortPotential()
                    } else {
                        this.potentialTimePeriod = [new Date(this.showingPeriod.getFullYear(), this.showingPeriod.getMonth(), day)]
                    }
                    this.togglePickerButton(true)
                    this.render(false)
                }
            }
        }
    }

    private onOver(event: MouseEvent): void {
        const target = event.target as HTMLElement | null
        if (!target || this.potentialTimePeriod.length != 1 || this.isYears()) {
            return
        }

        if (target.matches(this.selectors.datePickerItem)) {
            const targetDay = Number(target.dataset.date)
            if (targetDay) {
                const targetDate = new Date(this.showingPeriod.getFullYear(), this.showingPeriod.getMonth(), targetDay)
                this.rootElement.querySelectorAll(this.selectors.datePickerItem).forEach(item => {
                    const day = Number((item as HTMLElement).dataset.date)
                    if (day) {
                        const date = new Date(this.showingPeriod.getFullYear(), this.showingPeriod.getMonth(), day)
                        let isActive
                        if (targetDate.getTime() < this.potentialTimePeriod[0].getTime()) {
                            isActive = date.getTime() <= this.potentialTimePeriod[0].getTime() && date.getTime() >= targetDate.getTime()
                        } else {
                            isActive = date.getTime() >= this.potentialTimePeriod[0].getTime() && date.getTime() <= targetDate.getTime()
                        }
                        item.classList.toggle(this.stateSelectors.isActive.slice(1), isActive)
                    }
                })
            }
        }
    }

    private onLeave(event: MouseEvent): void {
        const target = event.target as HTMLElement | null
        if (!target || this.potentialTimePeriod.length != 1 || this.isYears()) {
            return
        }

        if (target.matches(this.selectors.datePickerList)) {
            this.rootElement.querySelectorAll(this.selectors.datePickerItem).forEach(item => {
                const day = Number((item as HTMLElement).dataset.date)
                if (day) {
                    const date = new Date(this.showingPeriod.getFullYear(), this.showingPeriod.getMonth(), day)
                    const isActive = DateGetter.isSameDay(date, this.potentialTimePeriod[0])
                    item.classList.toggle(this.stateSelectors.isActive.slice(1), isActive)
                }
            })
        }
    }

    private sortPotential(): void {
        if (this.potentialTimePeriod[0].getTime() < this.potentialTimePeriod[1].getTime()) {
            return
        }
        const temp = new Date(this.potentialTimePeriod[0])
        this.potentialTimePeriod[0] = new Date(this.potentialTimePeriod[1])
        this.potentialTimePeriod[0].setHours(0, 0, 0, 0)

        this.potentialTimePeriod[1] = temp
        this.potentialTimePeriod[1].setHours(23, 59, 59, 999)
    }

    private togglePickerButton(isActive: boolean) {
        const pickerButtonElement = this.rootElement.querySelector(this.selectors.datePickerButton) as HTMLElement | null
        if (!pickerButtonElement) {
            console.error("CalendarButton.ts (togglePickerButton): pickerButtonElement not found")
            return
        }
        pickerButtonElement.classList.toggle(this.stateSelectors.disabled.slice(1), !isActive)
    }

    private togglePickerYear(year: number) {
        const offset = (this.showingPeriod.getFullYear() - year) * 12
        let newShowingPeriod = DateGetter.subtractMonths(this.showingPeriod, offset)
        if (DateGetter.subtractMonths(this.showingPeriod, offset).getTime() > new Date().getTime()) {
            newShowingPeriod = new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0, 23, 59, 59, 999)
        }

        this.showingPeriod = newShowingPeriod
        this.render(false)
    }

    private toggleRootMonth(forward: boolean) {
        if (forward) {
            const arrowElement = this.rootElement.querySelector(this.selectors.calendarRightArrow) as HTMLElement | null
            if (!arrowElement) {
                console.error("CalendarButton.ts (toggleRootMonth): rightArrowElement not found")
                return
            }
            if (!arrowElement.matches(this.stateSelectors.isActive)) {
                return
            }

            const start = DateGetter.subtractMonths(this.store.period[1], -1)
            start.setDate(1)
            const end = DateGetter.subtractMonths(this.store.period[1], -1)
            end.setMonth(end.getMonth() + 1, 0)
            this.store.period = [start, end]
            if (this.store.period[1].getMonth() === new Date().getMonth() && this.store.period[1].getFullYear() === new Date().getFullYear()) {
                arrowElement.classList.remove(this.stateSelectors.isActive.slice(1))
            }
            // const isYears: boolean | null = this.isYears()
            // if (isYears != null) {
            //     this.render(isYears)
            // }
        } else {
            const arrowElement = this.rootElement.querySelector(this.selectors.calendarLeftArrow) as HTMLElement | null
            if (!arrowElement) {
                console.error("CalendarButton.ts (toggleRootMonth): leftArrowElement not found")
                return
            }
            if (!arrowElement.matches(this.stateSelectors.isActive)) {
                return
            }

            const start = DateGetter.subtractMonths(this.store.period[1], 1)
            start.setDate(1)
            const end = DateGetter.subtractMonths(this.store.period[1], 1)
            end.setMonth(end.getMonth() + 1, 0)
            this.store.period = [start, end]
            const rightArrowElement = this.rootElement.querySelector(this.selectors.calendarRightArrow) as HTMLElement | null
            if (!rightArrowElement) {
                console.error("CalendarButton.ts (toggleRootMonth): rightArrowElement not found")
                return
            }
            rightArrowElement.classList.add(this.stateSelectors.isActive.slice(1))

            // const isYears: boolean | null = this.isYears()
            // if (isYears != null) {
            //     this.render(isYears)
            // }
        }
    }

    private togglePickerMonth(forward: boolean) {
        if (forward) {
            const arrowElement = this.rootElement.querySelector(this.selectors.datePickerRightArrow) as HTMLElement | null
            if (!arrowElement) {
                console.error("CalendarButton.ts (togglePickerMonth): rightArrowElement not found")
                return
            }
            if (!arrowElement.matches(this.stateSelectors.isActive)) {
                return
            }

            this.showingPeriod = DateGetter.subtractMonths(this.showingPeriod, -1)
            if (this.showingPeriod.getMonth() === new Date().getMonth() && this.showingPeriod.getFullYear() === new Date().getFullYear()) {
                arrowElement.classList.remove(this.stateSelectors.isActive.slice(1))
            }
            const isYears: boolean | null = this.isYears()
            if (isYears != null) {
                this.render(isYears)
            }
        } else {
            const arrowElement = this.rootElement.querySelector(this.selectors.datePickerLeftArrow) as HTMLElement | null
            if (!arrowElement) {
                console.error("CalendarButton.ts (togglePickerMonth): leftArrowElement not found")
                return
            }
            if (arrowElement.matches(this.stateSelectors.isActive)) {
                this.showingPeriod = DateGetter.subtractMonths(this.showingPeriod, 1)
                if (this.showingPeriod.getMonth() !== new Date().getMonth() || this.showingPeriod.getFullYear() !== new Date().getFullYear()) {
                    const rightArrowElement = this.rootElement.querySelector(this.selectors.datePickerRightArrow) as HTMLElement | null
                    if (!rightArrowElement) {
                        console.error("CalendarButton.ts (togglePickerMonth): rightArrowElement not found")
                        return
                    }
                    rightArrowElement.classList.add(this.stateSelectors.isActive.slice(1))
                }

                const isYears: boolean | null = this.isYears()
                if (isYears != null) {
                    this.render(isYears)
                }
            }
        }
    }

    private isYears(): boolean {
        const datePickerElement = this.rootElement.querySelector(this.selectors.datePicker) as HTMLElement | null
        if (!datePickerElement) {
            console.error("CalendarButton.ts (isYears): datePickerElement not found")
            return false
        }

        return datePickerElement.matches(this.selectors.datePickerYears)
    }

    private fillRootTimePeriod() {
        const timePeriodElement = this.rootElement.querySelector(this.selectors.calendarTimePeriod) as HTMLElement | null
        if (!timePeriodElement) {
            console.error("CalendarButton.ts (fillTimePeriod): timePeriodElement not found)")
            return
        }

        if (DateGetter.isFullMonth(this.store.period[0], this.store.period[1])) {
            const year: string = new Date().getFullYear() === this.store.period[0].getFullYear() ? "" : ` ${this.store.period[0].getFullYear()}`
            timePeriodElement.textContent = DateGetter.translateMonth(this.store.period[0], false) + year
        } else {
            timePeriodElement.textContent = "Выбор периода"
        }
    }

    private fillPickerTimePeriod(isYears: boolean) {
        const monthElement = this.rootElement.querySelector(this.selectors.month) as HTMLElement | null
        const yearElement = this.rootElement.querySelector(this.selectors.year) as HTMLElement | null
        if (!monthElement) {
            console.error("CalendarButton.ts (fillPickerTimePeriod): monthElement not found")
            return
        }
        if (!yearElement) {
            console.error("CalendarButton.ts (fillPickerTimePeriod): yearElement not found")
            return
        }

        if (isYears) {
            monthElement.textContent = ''
        } else {
            monthElement.textContent = DateGetter.translateMonth(this.showingPeriod.getMonth(), false)
        }
        yearElement.classList.toggle(this.stateSelectors.isActive.slice(1), !isYears)
        yearElement.textContent = this.showingPeriod.getFullYear().toString()
    }

    private fillPickerDays() {
        const pickerListElement = this.rootElement.querySelector(this.selectors.datePickerList) as HTMLElement | null
        if (!pickerListElement) {
            console.error("CalendarButton.ts (fillPickerDays): pickerListElement not found")
            return
        }

        pickerListElement.innerHTML = ``
        const amountOfDays: number = DateGetter.getAmountOfDays(this.showingPeriod)
        for (let day = 1; day <= amountOfDays; day++) {
            const date = new Date(this.showingPeriod.getFullYear(), this.showingPeriod.getMonth(), day)
            let isActive = false
            if (this.potentialTimePeriod.length === 1) {
                isActive = DateGetter.isSameDay(date, this.potentialTimePeriod[0])
            } else if (this.potentialTimePeriod.length === 2) {
                isActive = this.potentialTimePeriod[0].getTime() <= date.getTime() && date.getTime() <= this.potentialTimePeriod[1].getTime()
            }

            const datePickerItem = document.createElement("li")
            datePickerItem.className = "date-picker__item" + (isActive ? ` ${this.stateSelectors.isActive.slice(1)}` : "")
            datePickerItem.dataset.date = day.toString()
            datePickerItem.textContent = day.toString()
            pickerListElement.append(datePickerItem)
        }
    }

    private fillPickerYears(from: number = 2009) {
        const datePickerElement = this.rootElement.querySelector(this.selectors.datePicker)
        const pickerListElement = this.rootElement.querySelector(this.selectors.datePickerList)
        if (!pickerListElement) {
            console.error("CalendarButton.ts (fillPickerYears): pickerListElement not found")
            return
        } else if (!datePickerElement) {
            console.error("CalendarButton.ts (fillPickerYears): datePickerElement not found")
            return
        }

        datePickerElement.classList.add(this.selectors.datePickerYears.slice(1))
        pickerListElement.innerHTML = ``
        DateGetter.getYears(from, new Date().getFullYear()).forEach(year => {
            const datePickerItem = document.createElement("li")
            datePickerItem.innerHTML = `
                <li class="date-picker__item" data-date="${year}">
                      ${year}
                </li>`
            pickerListElement.append(datePickerItem)
        })
    }

    private render(isYears: boolean) {
        const datePickerElement = this.rootElement.querySelector(this.selectors.datePicker) as HTMLElement | null
        datePickerElement?.classList.toggle(this.selectors.datePickerYears.slice(1), isYears)

        this.fillRootTimePeriod()
        this.fillPickerTimePeriod(isYears)
        isYears ? this.fillPickerYears() : this.fillPickerDays()

        const rightRootArrow = this.rootElement.querySelector(this.selectors.calendarRightArrow) as HTMLElement | null
        if (!rightRootArrow) {
            console.error("CalendarButton.ts (render): calendarArrow not found")
            return
        }
        let isActive = !DateGetter.isSameDay(this.store.period[1], new Date()) && this.store.period[1].getTime() < new Date().getTime()
        rightRootArrow.classList.toggle(this.stateSelectors.isActive.slice(1), isActive)

        const leftPickerArrow = this.rootElement.querySelector(this.selectors.datePickerLeftArrow) as HTMLElement | null
        const rightPickerArrow = this.rootElement.querySelector(this.selectors.datePickerRightArrow) as HTMLElement | null
        if (!leftPickerArrow || !rightPickerArrow) {
            console.error("CalendarButton.ts (render): pickerArrow not found")
            return
        }
        leftPickerArrow.classList.toggle(this.stateSelectors.isActive.slice(1), !isYears)
        isActive = (this.showingPeriod.getMonth() !== new Date().getMonth() || this.showingPeriod.getFullYear() !== new Date().getFullYear()) && !isYears
        rightPickerArrow.classList.toggle(this.stateSelectors.isActive.slice(1), isActive)

        const pickerButtonElement = this.rootElement.querySelector(this.selectors.datePickerButton) as HTMLElement | null
        if (!pickerButtonElement) {
            console.error("CalendarButton (render): pickerButtonElement not found")
            return
        }
        this.togglePickerButton(this.potentialTimePeriod.length !== 0)
    }

    private togglePicker(open: boolean) {
        const pickerElement = this.rootElement.querySelector(this.selectors.datePicker)
        if (!pickerElement) {
            console.error("CalendarButton (togglePicker): date-picker element not found")
            return
        }

        pickerElement.classList.toggle(this.stateSelectors.isActive.slice(1), open)
        if (open) {
            this.potentialTimePeriod = []
            this.showingPeriod = new Date(this.store.period[1])
            this.render(false)
        }
    }

    private pickTrainingsPeriod() {
        if (this.potentialTimePeriod.length === 1) {
            const date = new Date(this.potentialTimePeriod[0])
            date.setHours(23, 59, 59, 900)
            this.potentialTimePeriod.push(
                date
            )
        }
        this.store.period = [...this.potentialTimePeriod] as [Date, Date]
    }
}

export default CalendarButton