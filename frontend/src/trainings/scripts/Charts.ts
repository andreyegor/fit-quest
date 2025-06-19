/*
*
* Рендер через либу Chart.js и объекты data рисует графики в переданные HTMLCanvasElement.
*  Если данных нет - скипает.
*  Возвращает кол-во отображенных графиков
*
*/

import {
    CategoryScale,
    Chart,
    Legend,
    LinearScale,
    LineController,
    LineElement,
    PointElement,
    Title,
    Tooltip
} from "chart.js";

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

class Charts {
    private static readonly translations = {
        speedSeries: "Скорость",
        heartRateSeries: "Пульс",
    }

    private static createTimings(duration: number, seriesLength: number): number[] {
        const timings: number[] = []
        const step = duration / seriesLength
        for (let timing = 0; timing <= duration; timing += step) {
            timings.push(Number(timing.toFixed(2)))
        }
        return timings
    }

    private static createSmallChart(chart: HTMLCanvasElement, duration: number, series: number[]): void {
        new Chart(chart, {
            type: 'line',
            data: {
                labels: this.createTimings(duration, series.length),
                datasets: [{
                    data: series,
                    borderColor: 'rgb(162, 46, 160)',
                    tension: 0.1,
                    pointRadius: 0,
                    borderWidth: 1,
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {display: false},
                    tooltip: {enabled: false}
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
    }

    private static createBigChart(chart: HTMLCanvasElement, duration: number, series: number[], type: string): void {
        type = this.translations[type as keyof typeof this.translations]

        new Chart(chart, {
            type: "line",
            data: {
                labels: this.createTimings(duration, series.length),
                datasets: [{
                    label: type,
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
                            text: type
                        }
                    }
                }
            }
        })
    }

    public static renderSmall(charts: HTMLCanvasElement[], datas: {duration: number, series: number[]}[]): number {
        let counter = 0

        for (let i = 0; i < Math.min(charts.length, datas.length); i++) {
            if (datas[i].series.length === 0) continue
            this.createSmallChart(charts[i], datas[i].duration, datas[i].series)
            counter += 1
        }

        return counter
    }

    public static renderBig(charts: HTMLCanvasElement[], datas: {duration: number, seriesType: string, seriesValues: number[]}[]): number {
        let counter = 0

        for (let i = 0; i < Math.min(charts.length, datas.length); i++) {
            if (datas[i].seriesValues.length === 0) continue
            this.createBigChart(charts[i], datas[i].duration, datas[i].seriesValues, datas[i].seriesType)
            counter += 1
        }

        return counter
    }
}

export default Charts