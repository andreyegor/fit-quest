import {rawTrainingData, trainingElementData} from "./types.ts";
import safeRefresh from "./safeRefresh.ts";
import DateGetter from "./DateGetter.ts";
import {
    Chart,
    LineController,
    LineElement,
    PointElement,
    LinearScale,
    CategoryScale,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';

Chart.register(
    LineController,
    LineElement,
    PointElement,
    LinearScale,
    CategoryScale,
    Title,
    Tooltip,
    Legend
);

class Trainings {
    private selectors = {
        list: ".trainings__list",
        training: ".training",
        trainingDate: ".training__title-date",
        trainingStartTime: ".time-period__start",
        trainingEndTime: ".time-period__end",
        trainingTypeTime: ".training__title-type-time",
        overlay: ".overlay",
        overlayCloseButton: ".overlay__close-button",
        overlayInner: ".overlay__inner",
        overlaySummaryList: ".training__overlay-summary-list",
        overlayChartsList: ".training__overlay-charts-list",
        trainingsSummaryList: ".trainings__summary-list",
        trainingsSummaryTitle: ".trainings__summary-title",
        trainingsMoreButton: ".trainings__more-button",
        calendarButtonDate: ".calendar-button__time-period",
        calendarButtonLeftArrow: "#calendarButtonLeftArrow",
        calendarButtonRightArrow: "#calendarButtonRightArrow",
        chart: "canvas.chart"
    }
    private classes = {
        trainingAccent: "training--accent",
        overlayBig: "overlay--big"
    }
    private previewURLs = {
        running: "../../../trainings/running.svg",
        walking: "../../../trainings/walking.svg",
        cycling: "../../../trainings/cycling.svg", // TODO: Вернуть пути на место
        swimming: "/icons/swimming.svg"
    }
    private translations = {
        running: "Бег",
        walking: "Ходьба",
        cycling: "Велосипед",
        swimming: "Плавание",
        distanceMeters: "Дистанция",
        caloriesKcal: "Килокалории",
        speedSeries: "Скорость",
        heartRateSeries: "Пульс",
        steps: "Шаги",
        0: "Январь",
        1: "Февраль",
        2: "Март",
        3: "Апрель",
        4: "Май",
        5: "Июнь",
        6: "Июль",
        7: "Август",
        8: "Сентябрь",
        9: "Октябрь",
        10: "Ноябрь",
        11: "Декабрь",
    }
    private trainingElementsData: trainingElementData[] = []
    private offset = 0
    private startDate: Date
    private endDate: Date

    public constructor() {
        const date = new Date()
        this.startDate = new Date(date.getFullYear(), date.getMonth(), 1, 0, 0, 0)
        this.endDate = new Date(date.getFullYear(), date.getMonth(), 31, 0, 0, 0)
        this.render(this.startDate, this.endDate)
        this.bindEvents()
    }

    private bindEvents() {
        document.addEventListener("click", (event: MouseEvent) => {
            this.onClick(event)
        })
        document.addEventListener("click", (event: MouseEvent) => {
            const target = event.target as HTMLElement | null
            if (!target) {
                return
            }

            if (target.closest(this.selectors.calendarButtonLeftArrow)) {
                this.switchPeriod(false)
            } else if (target.closest(this.selectors.calendarButtonRightArrow)) {
                this.switchPeriod(true)
            } else if (target.closest(this.selectors.trainingsMoreButton)) {
                this.switchPeriod(false)
            }
        })
    }

    private onClick(event: MouseEvent) {
        const overlayElement = document.querySelector(this.selectors.overlay) as HTMLElement | null
        if (!overlayElement) {
            return
        }

        const target = event.target as HTMLElement | null
        if (!target) {
            return
        }

        if (target.closest(this.selectors.overlayCloseButton) || target.matches(this.selectors.overlay)) {
            this.closeOverlay(overlayElement)
            return
        }

        const trainingElement = target.closest(this.selectors.training) as HTMLElement | null
        if (!trainingElement) {
            return
        }

        const number = trainingElement.dataset.number
        if (this.trainingElementsData[Number(number)]) {
            this.openOverlay(overlayElement, this.trainingElementsData[Number(number)])
        }
    }

    private openOverlay(overlayElement: HTMLElement, data: trainingElementData) {
        const overlayInnerElement = overlayElement.querySelector(this.selectors.overlayInner) as HTMLElement | null
        if (!overlayInnerElement) {
            return
        }

        overlayElement.classList.add("is-active")

        const overlayDateElement = overlayElement.querySelector(this.selectors.trainingDate) as HTMLElement | null
        if (overlayDateElement) {
            overlayDateElement.textContent = data.date
        }

        const overlayStartElement = overlayElement.querySelector(this.selectors.trainingStartTime) as HTMLElement | null
        if (overlayStartElement) {
            overlayStartElement.textContent = data.start
        }

        const overlayEndElement = overlayElement.querySelector(this.selectors.trainingEndTime) as HTMLElement | null
        if (overlayEndElement) {
            overlayEndElement.textContent = data.end
        }

        const overlayTypeTimeElement = overlayElement.querySelector(this.selectors.trainingTypeTime) as HTMLElement | null
        if (overlayTypeTimeElement) {
            overlayTypeTimeElement.textContent = `${data.type}, ${data.duration} минут`
        }

        const overlaySummaryListElement = overlayElement.querySelector(this.selectors.overlaySummaryList) as HTMLElement | null
        if (overlaySummaryListElement) {
            overlaySummaryListElement.innerHTML = ""
            data.metrics.filter(metric => metric.length === 2).forEach(metric => {
                const summaryItemElement = document.createElement("li")
                summaryItemElement.classList.add("trainings__summary-item", "training__overlay-summary-item")

                const summaryTypeElement = document.createElement("span")
                summaryTypeElement.classList.add("trainings__summary-type")
                summaryTypeElement.textContent = metric[0].slice(2, metric[0].length - 2)
                summaryItemElement.append(summaryTypeElement)

                const summaryValueElement = document.createElement("span")
                summaryValueElement.classList.add("trainings__summary-value")
                summaryValueElement.textContent = metric[1]
                summaryItemElement.append(summaryValueElement)

                overlaySummaryListElement.append(summaryItemElement)
            })
        }

        const overlayChartsListElement = overlayElement.querySelector(this.selectors.overlayChartsList) as HTMLElement | null
        if (overlayChartsListElement) {
            overlayChartsListElement.innerHTML = ""
            Object.entries(data.series).forEach(([type, series]) => {
                const chartsItemElement = document.createElement("li")
                chartsItemElement.classList.add("training__overlay-charts-item")

                const chartElement = document.createElement("canvas")
                chartElement.classList.add("chart")

                const label = this.translations[type as keyof typeof this.translations]
                const interval: number = (data.rawEnd.getTime() - data.rawStart.getTime()) / series.length
                const timings: number[] = []
                for (let timing: number = 0; (timing < data.rawEnd.getTime() - data.rawStart.getTime() && timing >= 0); timing += interval) {
                    timings.push(Number((timing / 60000).toFixed(2)))
                }
                // drawChart(series, timings, label, chartElement, true)
                new Chart(chartElement, {
                    type: "line",
                    data: {
                        labels: timings,
                        datasets: [{
                            label: label,
                            data: series,
                            fill: false,
                            borderColor: 'rgb(162, 46, 160)',
                            tension: 0.1
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        scales: {
                            x: {
                                title: {
                                    display: true,
                                    text: 'Время (минуты)',
                                }
                            },
                            y: {
                                title: {
                                    display: true,
                                    text: label
                                }
                            }
                        }
                    }
                })

                chartsItemElement.append(chartElement)
                overlayChartsListElement.append(chartsItemElement)
            })
        }

        const overlayInnerHeight = overlayInnerElement.scrollHeight
        if (overlayInnerHeight > window.innerHeight * 0.9) {
            overlayElement.classList.add(this.classes.overlayBig)
        } else {
            overlayElement.classList.remove(this.classes.overlayBig)
            document.documentElement.classList.add("is-lock")
        }
        window.scrollTo(0, 0)
    }

    private closeOverlay(overlayElement: HTMLElement) {
        overlayElement.classList.remove("is-active")
        document.documentElement.classList.remove("is-lock")
    }

    private filterDataByDate(datas: trainingElementData[], start: Date = new Date(1970, 0, 1, 0, 0, 0), end: Date = new Date(9999, 11, 31, 23, 59, 59)): trainingElementData[] {
        return datas.filter(data => {
            if (data.rawStart.getTime() < start.getTime()) {
                return false
            } else if (data.rawEnd.getTime() > end.getTime()) {
                return false
            }
            return true
        })
    }

    private periodSummary(start: Date = new Date(1970, 0, 1, 0, 0, 0), end: Date = new Date(9999, 11, 31, 23, 59, 59)) {
        const trainingsSummaryListElement = document.querySelector(this.selectors.trainingsSummaryList) as HTMLElement | null
        if (!trainingsSummaryListElement) {
            return
        }

        const trainingsSummaryTitleElement = document.querySelector(this.selectors.trainingsSummaryTitle)
        if (trainingsSummaryTitleElement) {
            trainingsSummaryTitleElement.textContent = `Результаты за ${this.translations[start.getMonth() as keyof typeof this.translations]}`
        }

        const datas = this.filterDataByDate(this.trainingElementsData, start, end)

        const metrics: { [key: string]: number } = {}
        datas.forEach(data => {
            data.metrics.forEach(metric => {
                if (metrics[metric[0]]) {
                    metrics[metric[0]] += Number(metric[1])
                } else {
                    metrics[metric[0]] = Number(metric[1])
                }
            })
        })

        trainingsSummaryListElement.innerHTML = Object.keys(metrics).length === 0 ? `<li class="trainings__summary-item">Ничего не найдено</li>` : ""

        Object.entries(metrics).forEach(([type, value]) => {
            const trainingsSummaryItem = document.createElement("li")
            trainingsSummaryItem.classList.add("trainings__summary-item")

            const trainingsSummaryType = document.createElement("span")
            trainingsSummaryType.classList.add("trainings__summary-type")
            trainingsSummaryType.textContent = type.slice(2, type.length - 2)

            const trainingsSummaryValue = document.createElement("span")
            trainingsSummaryValue.classList.add("trainings__summary-value")
            trainingsSummaryValue.textContent = value.toString()

            trainingsSummaryItem.append(trainingsSummaryType, trainingsSummaryValue)
            trainingsSummaryListElement.append(trainingsSummaryItem)
        })
    }

    private makeATrainingElement(data: trainingElementData, id: number, classes: string[] = []): HTMLElement {
        const trainingElement = document.createElement("li")
        trainingElement.classList.add("trainings__item", "training", ...classes)
        trainingElement.dataset.number = `${id}`
        const previewElement = document.createElement("span")
        previewElement.classList.add("training__preview")
        const previewImageElement = document.createElement("img")
        previewImageElement.setAttribute("src", data.previewURL)
        previewImageElement.setAttribute("loading", "lazy")
        previewElement.append(previewImageElement)
        trainingElement.append(previewElement)
        const descriptionElement = document.createElement("div")
        descriptionElement.classList.add("training__description")
        const titleElement = document.createElement("div")
        titleElement.classList.add("training__title")
        const dateWrapperElement = document.createElement("div")
        dateWrapperElement.classList.add("training__title-date-wrapper")
        const dateElement = document.createElement("time")
        dateElement.classList.add("training__title-date")
        dateElement.textContent = data.date
        dateWrapperElement.append(dateElement)
        const timeElement = document.createElement("div")
        timeElement.classList.add("training__title-time-period", "time-period")
        const timeStartElement = document.createElement("span")
        timeStartElement.classList.add("time-period__start")
        timeStartElement.textContent = data.start
        timeElement.append(timeStartElement)
        const timeEndElement = document.createElement("span")
        timeEndElement.classList.add("time-period__end")
        timeEndElement.textContent = data.end
        timeElement.append(timeEndElement)
        dateWrapperElement.append(timeElement)
        titleElement.append(dateWrapperElement)
        const generalInfoElement = document.createElement("span")
        generalInfoElement.classList.add("training__title-type-time")
        generalInfoElement.textContent = `${data.type}, ${data.duration} минут`
        titleElement.append(generalInfoElement)

        const additionalListElement = document.createElement("ul")
        additionalListElement.classList.add("training__additional-list")

        data.metrics = data.metrics.filter(metric => metric.length === 2)
        for (let i = 0; i < 2 && i < data.metrics.length; i++) {
            const metric = data.metrics[i]
            const additionalItemElement = document.createElement("li")
            additionalItemElement.classList.add("training__additional-item")

            const additionalTypeElement = document.createElement("span")
            additionalTypeElement.classList.add("training__additional-type", "h5")
            additionalTypeElement.textContent = metric[0]
            additionalItemElement.append(additionalTypeElement)

            const additionalValueElement = document.createElement("span")
            additionalValueElement.classList.add("training__additional-value", "h5")
            additionalValueElement.textContent = metric[1]
            additionalTypeElement.append(additionalValueElement)

            additionalListElement.append(additionalItemElement)
        }

        descriptionElement.append(titleElement)
        descriptionElement.append(additionalListElement)
        trainingElement.append(descriptionElement)
        const chartsListElement = document.createElement("ul")
        chartsListElement.classList.add("training__charts-list", "hidden-mobile")
        Object.values(data.series).forEach((series) => {
            const chartsItemElement = document.createElement("li")
            chartsItemElement.classList.add("training__charts-item")

            const chartElement = document.createElement("canvas")
            chartElement.classList.add("chart")

            const interval: number = (data.rawEnd.getTime() - data.rawStart.getTime()) / series.length
            const timings: number[] = []
            for (let timing: number = 0; (timing < data.rawEnd.getTime() - data.rawStart.getTime() && timing >= 0); timing += interval) {
                timings.push(Number((timing / 60000).toFixed(2)))
            }
            new Chart(chartElement, {
                type: 'line',
                data: {
                    labels: timings,
                    datasets: [{
                        data: series,
                        borderColor: 'rgb(162, 46, 160)',
                        tension: 0.1,
                        pointRadius: 0,        // без точек
                        borderWidth: 1,      // тонкая линия
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {display: false}, // убираем легенду
                        tooltip: {enabled: false} // убираем всплывающие подсказки
                    },
                    scales: {
                        x: {
                            display: false,
                            ticks: {
                                display: false
                            }
                        },
                        y: {
                            display: false,
                            ticks: {
                                display: false
                            }
                        }
                    },
                    elements: {
                        line: {
                            borderJoinStyle: 'round'
                        }
                    }
                }
            })

            chartsItemElement.append(chartElement)
            chartsListElement.append(chartsItemElement)
        })
        trainingElement.append(chartsListElement)
        return trainingElement
    }

    private async getData({types, offset, limit}: {
        types?: string[],
        offset?: number,
        limit?: number
    } = {}): Promise<trainingElementData[]> {
        let fetchURL: string = "https://fit-quest.ru/api/exercises?"
        if (types && types?.length !== 0) {
            fetchURL += `types=${types?.join(",")}`
        }
        if (offset) {
            fetchURL += `${fetchURL.at(-1) === "?" ? "" : "&"}offset=${offset}`
        }
        if (limit) {
            fetchURL += `${fetchURL.at(-1) === "?" ? "" : "&"}limit=${limit}`
        }

        try {
            const response = await fetch(fetchURL, {
                method: "GET",
                credentials: "include"
            })

            if (response.ok || (response.status === 401 && await safeRefresh())) {
                // if (response.ok) {
                const trainingElementsData: trainingElementData[] = []
                const data = await response.json() as rawTrainingData[]
                data.forEach(rawData => {
                    const startDate = new Date(rawData.startTime)
                    const endDate = new Date(rawData.endTime)

                    const id = this.trainingElementsData.length
                    const start = DateGetter.getLocalTime(startDate)
                    const date = DateGetter.getLocalDate(startDate)
                    const end = DateGetter.getLocalTime(endDate)
                    const duration = DateGetter.getDifferenceInDates(startDate, endDate)
                    const previewURL = this.previewURLs[rawData.exerciseType]
                    const type = this.translations[rawData.exerciseType]

                    const metrics: string[][] = []
                    const translations = this.translations
                    Object.entries(rawData.metrics).forEach(([type, value]) => {
                        const newType = type as keyof typeof translations
                        let metricType = `- ${translations[newType]}: `
                        let metricValue = `${value}`
                        metrics.push([metricType, metricValue])
                    })

                    this.trainingElementsData.push({
                        id: id,
                        date: date,
                        type: type,
                        duration: duration,
                        start: start,
                        end: end,
                        metrics: metrics,
                        series: rawData.series,
                        previewURL: previewURL,
                        rawStart: startDate,
                        rawEnd: endDate
                    })
                    trainingElementsData.push({
                        id: id,
                        date: date,
                        type: type,
                        duration: duration,
                        start: start,
                        end: end,
                        metrics: metrics,
                        series: rawData.series,
                        previewURL: previewURL,
                        rawStart: startDate,
                        rawEnd: endDate
                    })
                })
                return trainingElementsData
            } else {
                const message: string = await response.text()
                console.error(`[Log] Error ${response.status} ${message ? `: ${message}` : ""}`)
                return []
            }
        } catch (e: any) {
            console.error(`[Log] Network error${e.message ? `: ${e.message}` : ''}`)
            return []
        }
    }

    public async render(start: Date = new Date(1970, 0, 1, 0, 0, 0), end: Date = new Date(9999, 11, 31, 23, 59, 59)) {

        const trainingListElement = document.querySelector(this.selectors.list)
        if (!trainingListElement) {
            console.warn("[Trainings]: couldn't find .training__list element")
            return
        }

        let newData: trainingElementData[] = await this.getData({offset: this.offset, limit: 5})
        this.offset = this.trainingElementsData.length

        while (this.filterDataByDate(newData, start, end).length !== 0) {
            newData = await this.getData({offset: this.offset, limit: 5})
            this.offset = this.trainingElementsData.length
        }

        const datas = this.filterDataByDate(this.trainingElementsData, start, end)
        trainingListElement.innerHTML = datas.length === 0 ? `<li class="trainings__item trainings__more h4">Ничего не найдено</li>` : ""
        datas.forEach((data, index) => {
            if (index % 2 == 1) {
                trainingListElement.append(
                    this.makeATrainingElement(data, data.id, [this.classes.trainingAccent])
                )
            } else {
                trainingListElement.append(this.makeATrainingElement(data, data.id))
            }
        })

        const translations = this.translations
        const month = start.getMonth() as keyof typeof translations
        const year = start.getFullYear()
        this.addMoreButton(this.translations[month], year)
        this.settingCalendarButton(this.translations[month])
        this.periodSummary(start, end)
    }

    private settingCalendarButton(month: string) {
        const calendarButton = document.querySelector(this.selectors.calendarButtonDate)
        if (calendarButton) {
            calendarButton.textContent = month
        }
    }

    private addMoreButton(month: string, year: number) {
        const trainingListElement = document.querySelector(this.selectors.list)
        if (!trainingListElement) {
            console.warn("[Trainings]: couldn't find .training__list element")
            return
        }
        const trainingItemElement = document.createElement("li")
        trainingItemElement.classList.add("trainings__item", "trainings__more")
        const moreButtonElement = document.createElement("button")
        moreButtonElement.classList.add("trainings__more-button", "button", "button--accent")
        const buttonTextElement = document.createElement("span")
        buttonTextElement.classList.add("h5")
        buttonTextElement.textContent = `Тренировки за ${month} ${year}`
        moreButtonElement.append(buttonTextElement)
        trainingItemElement.append(moreButtonElement)
        trainingListElement.append(trainingItemElement)
    }

    private switchPeriod(forward: boolean) {
        const moreButtonElement = document.querySelector(this.selectors.trainingsMoreButton)
        if (moreButtonElement) {
            moreButtonElement.remove()
        }

        let offset = forward ? 1 : -1
        this.startDate.setMonth(this.startDate.getMonth() + offset)
        this.endDate.setMonth(this.endDate.getMonth() + offset)

        this.render(this.startDate, this.endDate)
    }

    // private drawAllCharts() {
    //     const canvasElements = document.querySelectorAll(this.selectors.chart) as NodeListOf<HTMLCanvasElement>
    //     if (canvasElements.length === 0) return
    //
    //     canvasElements.forEach(canvasElement => {
    //         const parent = canvasElement.closest(this.selectors.training) as HTMLElement
    //         const id = parent?.dataset?.number
    //         if (id) {
    //             const data = this.trainingElementsData.at(Number(id))
    //             if (data) {
    //                 Object.entries(data.series).forEach(series => {
    //
    //                 })
    //             }
    //         }
    //     })
    // }
}

export default Trainings