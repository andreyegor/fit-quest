import {TrainingElementData} from "../../scripts/types.ts";
import Charts from "./Charts.ts";

class Training {
    private selectors = {
        overlay: ".overlay",
        overlaySummaryList: ".training__overlay-summary-list",
        overlayChartsList: ".training__overlay-charts-list",
        oneColumnOverlayChartsList: ".training__overlay-charts-list--1",
        trainingAdditionalList: ".training__additional-list",
        trainingChartsList: ".training__charts-list"
    }
    private stateSelectors = {
        isActive: ".is-active",
        isLock: ".is-lock"
    }
    private trainingElement!: HTMLElement
    private overlayElement!: HTMLElement
    private chartsCounter = 0

    constructor(data: TrainingElementData) {
        this.trainingElement = this.makeATrainingElement(data)
        this.overlayElement = this.makeAnOverlayElement(data)
    }

    public get(): HTMLElement {
        return this.trainingElement
    }

    public toggleOverlay(open: boolean): void {
        const overlayElement = document.querySelector(this.selectors.overlay)
        if (!overlayElement) {
            console.error("Training.ts (toggleOverlay): overlayElement not found")
            return
        }

        overlayElement.classList.toggle(this.stateSelectors.isActive.slice(1), open)
        document.documentElement.classList.toggle(this.stateSelectors.isLock.slice(1), open)
        if (open) {
            overlayElement.innerHTML = ``
            overlayElement.append(this.overlayElement)
        }
    }

    private makeATrainingElement(data: TrainingElementData): HTMLElement {
        const trainingElement = document.createElement("li")
        trainingElement.className = `trainings__item training${data.id % 2 === 0 ? ` training--accent` : ""}`
        trainingElement.dataset.id = data.id.toString()
        trainingElement.innerHTML = `
              <span class="training__preview">
                <img src="${data.previewURL}" loading="lazy" alt="">
              </span>
              <div class="training__description">
                <div class="training__title">
                  <div class="training__title-date-wrapper">
                    <time class="training__title-date">${data.date}</time>
                    <div class="training__title-time-period time-period">
                      <span class="time-period__start">${data.start}</span>
                      <span class="time-period__end">${data.end}</span>
                    </div>
                  </div>
                  <span class="training__title-type-time">${data.type}, ${data.duration} минут</span>
                </div>
                <ul class="training__additional-list"></ul>
              </div>
              <ul class="training__charts-list hidden-mobile">
<!--                <li class="training__charts-item">-->
<!--                  <canvas class="chart"></canvas>-->
<!--                </li>-->
<!--                <li class="training__charts-item">-->
<!--                  <canvas class="chart"></canvas>-->
<!--                </li>-->
              </ul>`

        const additionalListElement = trainingElement.querySelector(this.selectors.trainingAdditionalList)
        data.metrics.forEach(metric => {
            const metricType: string = metric[0]
            const metricValue: string = metric[1]

            const additionalItemElement = document.createElement("li")
            additionalItemElement.className = "training__additional-item"
            additionalItemElement.innerHTML = `
                <span class="training__additional-type h5">- ${metricType}: <span class="training__additional-value h5">${metricValue}</span></span>`
            additionalListElement?.append(additionalItemElement)
        })

        const chartsListElement = trainingElement.querySelector(this.selectors.trainingChartsList)
        Object.values(data.series).forEach(series => {
            const chartElement: HTMLCanvasElement = document.createElement("canvas")
            if (Charts.renderSmall([chartElement], [{series: series, duration: data.duration}]) === 1 && chartsListElement) {
                const chartsItemElement = document.createElement("li")
                chartsItemElement.className = "training__charts-item"
                chartsItemElement.append(chartElement)
                chartsListElement.append(chartsItemElement)
                this.chartsCounter += 1
            }
        })
        return trainingElement
    }

    private makeAnOverlayElement(data: TrainingElementData): HTMLElement {
        const overlayElement = document.createElement("div")
        overlayElement.className = "overlay__inner training__overlay"
        overlayElement.dataset.id = data.id.toString()
        overlayElement.innerHTML = `
              <button class="overlay__close-button close-button" data-id="${data.id}"></button>
              <div class="training__overlay-title training__title">
                <div class="training__title-date-wrapper">
                  <time class="training__title-date">${data.date}</time>
                  <div class="training__title-time-period time-period">
                    <span class="time-period__start">${data.start}</span>
                    <span class="time-period__end">${data.end}</span>
                  </div>
                </div>
                <span class="training__title-type-time">${data.type}, ${data.duration} минут</span>
              </div>
              <div class="training__overlay-summary">
                <h2 class="trainings__summary-title training__overlay-summary-title">Результаты</h2>
                <ul class="trainings__summary-list training__overlay-summary-list"></ul>
              </div>
              <ul class="training__overlay-charts-list"></ul>`

        const summaryListElement = overlayElement.querySelector(this.selectors.overlaySummaryList)
        data.metrics.forEach(metric => {
            const metricType: string = metric[0]
            const metricValue: string = metric[1]

            const additionalItemElement = document.createElement("li");
            additionalItemElement.className = "trainings__summary-item training__overlay-summary-item"
            additionalItemElement.innerHTML = `
                <span class="trainings__summary-type">${metricType}</span><span class="trainings__summary-value">${metricValue}</span>`
            summaryListElement?.append(additionalItemElement)
        })

        const chartsListElement = overlayElement.querySelector(this.selectors.overlayChartsList)
        Object.entries(data.series).forEach(entry => {
            const seriesType = entry[0]
            const seriesValues = entry[1]

            const chartsItemElement = document.createElement("li")
            chartsItemElement.className = "training__overlay-charts-item"
            const chartElement: HTMLCanvasElement = document.createElement("canvas")
            if (Charts.renderBig([chartElement], [{seriesType: seriesType, seriesValues: seriesValues, duration: data.duration}]) === 1 && chartsListElement) {
                chartsItemElement.append(chartElement)
                chartsListElement.append(chartsItemElement)
            }
        })
        chartsListElement?.classList.toggle(this.selectors.oneColumnOverlayChartsList.slice(1), this.chartsCounter < 2)

        return overlayElement
    }
}

export default Training