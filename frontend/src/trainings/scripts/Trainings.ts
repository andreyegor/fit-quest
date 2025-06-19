/*
*
* Backlog:
*  - при выборе 1 дня summaryTitle дублирует его.
*  - когда треньки кончились пишет об этом после повторного нажатия на "показать ещё"
*
*/

import {TrainingData} from "../../scripts/types.ts";
import ApiRequests from "../../scripts/ApiRequests.ts";
import DateGetter from "../../scripts/DateGetter.ts";
import Training from "./Training.ts";
import TimePeriodStore from "../../scripts/TimePeriodStore.ts";

export class Trainings {
    private readonly selectors = {
        root: ".trainings__list",
        summaryTitle: ".trainings__summary-title",
        summaryList: ".trainings__summary-list",
        training: ".training",
        overlay: ".overlay",
        overlayCloseButton: ".overlay__close-button",
        moreButton: ".trainings__more-button",
        loadMoreButton: "#loadMoreButton",
        prevMonthButton: "#prevMonthButton",
        thisMonthButton: "#thisMonthButton",
        newMonthButton: "#newMonthButton",
        calendarButton: ".trainings__calendar-button.calendar-button"
    }
    private readonly translations = {
        cycling: "Велопробежка",
        running: "Бег",
        walking: "Ходьба",
        swimming: "Плавание",
        steps: "Шаги",
        distanceMeters: "Дистанция",
        caloriesKcal: "Килокалории",
    }
    private readonly URLs = {
        running: "/icons/running.svg",
        walking: "/icons/walking.svg",
        cycling: "/icons/cycling.svg",
        swimming: "/icons/swimming.svg"
    }
    private readonly limit: number = 10

    private store: TimePeriodStore
    private summaryMetrics: { [key: string]: number } = {}
    private trainings: Training[] = []
    private readAccess: boolean = true
    private types: number[] = [] // all

    constructor(store: TimePeriodStore) {
        this.store = store
        store.subscribe(() => {
            this.summaryMetrics = {}
            this.readAccess = true
            this.cleanTrainings()
            this.render()
        })
        this.bindEvents()
        this.render()
    }

    private async bindEvents() {
        document.addEventListener("click", (event: MouseEvent) => this.onClick(event))
    }

    private async onClick(event: MouseEvent) {
        const target = event.target as HTMLElement | null
        if (!target) return

        const trainingElement = target.closest(this.selectors.training) as HTMLElement | null
        if (trainingElement && trainingElement.dataset.id) {
            this.trainings[Number(trainingElement.dataset.id) - 1].toggleOverlay(true)
            return
        }

        const overlayElement = target.closest(this.selectors.overlay) as HTMLElement | null
        if (overlayElement) {
            const overlayCloseButtonElement = overlayElement.querySelector(this.selectors.overlayCloseButton) as HTMLElement | null
            if (overlayCloseButtonElement && overlayCloseButtonElement.dataset.id) {
                this.trainings[Number(overlayCloseButtonElement.dataset.id) - 1].toggleOverlay(false)
                return
            }
        }

        const overlayCloseButtonElement = target.closest(this.selectors.overlayCloseButton) as HTMLElement | null
        if (overlayCloseButtonElement && overlayCloseButtonElement.dataset.id) {
            this.trainings[Number(overlayCloseButtonElement.dataset.id) - 1].toggleOverlay(false)
            return
        }

        const moreButton = target.closest(this.selectors.moreButton) as HTMLElement | null
        if (moreButton) {
            if (moreButton.matches(this.selectors.newMonthButton)) {
                this.pickTimePeriod(
                    new Date(new Date().getFullYear(), new Date().getMonth(), 1),
                    new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0, 23, 59, 59, 999)
                )
            } else if (moreButton.matches(this.selectors.thisMonthButton)) {
                this.pickTimePeriod(
                    new Date(this.store.period[1].getFullYear(), this.store.period[1].getMonth(), 1),
                    new Date(this.store.period[1].getFullYear(), this.store.period[1].getMonth() + 1, 0, 23, 59, 59, 999)
                )
            } else if (moreButton.matches(this.selectors.prevMonthButton)) {
                this.pickTimePeriod(
                    new Date(this.store.period[0].getFullYear(), this.store.period[0].getMonth() - 1, 1),
                    new Date(this.store.period[1].getFullYear(), this.store.period[1].getMonth(), 0, 23, 59, 59, 999)
                )
            } else if (moreButton.matches(this.selectors.loadMoreButton)) {
                await this.render()
            }
            return
        }
    }

    private makeAnEndButton(): HTMLButtonElement {
        let textContent: string
        let id: string

        if (this.readAccess) {
            id = this.selectors.loadMoreButton.slice(1)
            textContent = `Показать ещё`
        } else {
            const isCurrentYear = new Date().getFullYear() === this.store.period[1].getFullYear()
            let addition = isCurrentYear ? "" : ` ${this.store.period[1].getFullYear()}`
            if (this.store.period[0].getFullYear() != this.store.period[1].getFullYear()) {
                id = this.selectors.newMonthButton.slice(1)
                textContent = `Тренировки за ${DateGetter.translateMonth(new Date().getMonth(), false)} ${new Date().getFullYear()}`
            } else if (DateGetter.isFullMonth(this.store.period[0], this.store.period[1])) {
                const newEndDate = new Date(this.store.period[1])
                newEndDate.setMonth(this.store.period[1].getMonth() - 1)
                if (this.store.period[1].getFullYear() !== newEndDate.getFullYear()) {
                    addition = ` ${newEndDate.getFullYear()}`
                }
                id = this.selectors.prevMonthButton.slice(1)
                textContent = `Тренировки за ${DateGetter.translateMonth(this.store.period[1].getMonth() - 1, false)}${addition}`
            } else {
                id = this.selectors.thisMonthButton.slice(1)
                textContent = `Тренировки за ${DateGetter.translateMonth(this.store.period[1].getMonth(), false)}${addition}`
            }
        }

        const buttonElement = document.createElement("button")
        buttonElement.className = "trainings__more-button button button--accent"
        buttonElement.id = id
        buttonElement.innerHTML = `<span class="h5">${textContent}</span>`

        return buttonElement
    }

    private async fillTrainingList() {
        const trainingsListElement = document.querySelector(this.selectors.root)
        const trainingListLength = this.trainings.length
        if (trainingsListElement) {
            const newDataLength = await this.getData()
            if (newDataLength === 0) {
                if (trainingListLength === 0) {
                    const infoItemElement = document.createElement("li")
                    infoItemElement.className = "trainings__item trainings__more h4"
                    infoItemElement.textContent = "Ничего не найдено"
                    trainingsListElement.append(infoItemElement)
                }
            } else {
                console.log(newDataLength, this.trainings)
                for (let i = 0; i < (newDataLength); i++) {
                    trainingsListElement.append(this.trainings[trainingListLength + i].get())
                }
            }

            const buttonItemElement = document.createElement("li")
            buttonItemElement.className = "trainings__item trainings__more"
            buttonItemElement.append(this.makeAnEndButton())
            trainingsListElement.querySelectorAll(this.selectors.moreButton)?.forEach(el => el.parentElement?.remove())
            trainingsListElement.append(buttonItemElement)
        } else {
            console.error("Trainings (render): trainingListElement not found")
        }
    }

    private fillSummaryTitle() {
        const summaryTitleElement = document.querySelector(this.selectors.summaryTitle) as HTMLElement | null
        if (!summaryTitleElement) {
            console.error("Trainings (fillSummaryTitle): summaryTitleElement not found")
            return
        }
        summaryTitleElement.innerHTML = DateGetter.translatePeriod(this.store.period[0], this.store.period[1], "Результаты за ")
    }

    private fillSummaryList() {
        // TODO: добавить запрос на апи

        const summaryListElement = document.querySelector(this.selectors.summaryList) as HTMLElement | null
        if (summaryListElement) {
            summaryListElement.innerHTML = ``
            if (Object.entries(this.summaryMetrics).length === 0) {
                const itemElement = document.createElement("li")
                itemElement.className = "trainings__summary-item"
                itemElement.textContent = "Ничего не найдено"
                summaryListElement.append(itemElement)
            } else {
                Object.entries(this.summaryMetrics).forEach(entry => {
                    const itemElement = document.createElement("li")
                    itemElement.className = "trainings__summary-item"
                    itemElement.innerHTML = `
                      <span class="trainings__summary-type">${entry[0]}</span>
                      <span class="trainings__summary-value">${entry[1]}</span>`
                    summaryListElement.append(itemElement)
                })
            }
        } else {
            console.error("Trainings (render): summaryListElement not found")
        }
    }

    private cleanTrainings() {
        const trainingListElement = document.querySelector(this.selectors.root)
        if (!trainingListElement) {
            console.error("Trainings (cleanTrainings): trainingListElement not found")
            return
        }
        trainingListElement.innerHTML = ``
        this.trainings = []
    }

    private pickTimePeriod(date1: Date, date2: Date): void {
        if (date1 > date2) {
            console.error("Trainings pickTimePeriod: date1 can't be greater than date2")
        } else {
            this.store.period = [date1, date2]
        }
    }

    private async getData(): Promise<number> {
        if (!this.readAccess) return 0

        const rawData: TrainingData[] | null = await ApiRequests.getTrainingData(
            {offset: this.trainings.length, timePeriod: this.store.period, types: this.types}
        )
        if (!rawData) return 0
        if (rawData.length === 0) {
            this.readAccess = false
            return 0
        }

        let counter = 0
        rawData.forEach((trainingData) => {
            const startDate: Date = new Date(trainingData.startTime)
            const endDate: Date = new Date(trainingData.endTime)

            const isInPeriod = startDate.getTime() <= this.store.period[1].getTime() && startDate.getTime() >= this.store.period[0].getTime()
            if (isInPeriod) {
                const metrics: string[][] = []
                Object.entries(trainingData.metrics).forEach(entry => {
                    const metricType = this.translations[entry[0] as keyof typeof this.translations]
                    metrics.push([
                        metricType, entry[1].toString()
                    ])

                    // TODO: заменить на запрос на апи в fillSummaryList
                    if (!this.summaryMetrics[metricType]) {
                        this.summaryMetrics[metricType] = 0
                    }
                    this.summaryMetrics[metricType] += entry[1]
                })

                const data = {
                    id: this.trainings.length + 1,
                    date: DateGetter.getLocalDate(startDate),
                    type: this.translations[trainingData.exerciseType as keyof typeof this.translations],
                    duration: DateGetter.getDifferenceInDates(startDate, endDate),
                    start: DateGetter.getLocalTime(startDate),
                    end: DateGetter.getLocalTime(endDate),
                    metrics: metrics,
                    series: trainingData.series,
                    previewURL: this.URLs[trainingData.exerciseType],
                }

                this.trainings.push(new Training(data))
                counter += 1
                // TODO: вернуть как было когда появится сортировка по дате на апи
            }
        })

        this.readAccess = false
        return counter
        // return rawData.length
    }

    private async render() {
        await this.fillTrainingList()
        this.fillSummaryTitle()
        this.fillSummaryList()
    }
}

export default Trainings;