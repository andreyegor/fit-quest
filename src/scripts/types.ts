export type ApiResponse = {status: "ok" | "fail", message: string}

export type RegisterCredentials = {
    email: string,
    username: string,
    password: string,
    passwordAgain: string
}

export type LoginCredentials = {
    email: string,
    password: string,
    // rememberMe: boolean
}

export type FormInputElement = HTMLInputElement | HTMLTextAreaElement;

export type trainingElementData = {
    date: string,
    type: string,
    duration: number,
    start: string,
    end: string,
    metrics: string[][],
    series: {
        [key: string]: number[]
    },
    previewURL: string
}

export type rawTrainingData = {
    endTime: string,
    metrics: {
        [key: string]: number
    },
    series: {
        [key: string]: number[]
    },
    startTime: string,
    exerciseType: "running" | "walking" | "cycling" | "swimming"
}