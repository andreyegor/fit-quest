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

export type UserInfo = {
    id: string,
    name: string
    email: string,
}

export type FormInputElement = HTMLInputElement | HTMLTextAreaElement;

export type TrainingElementData = {
    id: number,
    date: string,
    type: string,
    duration: number,
    start: string,
    end: string,
    metrics: string[][],
    series: {
        [key: string]: number[]
    },
    previewURL: string,
    // rawStart: Date,
    // rawEnd: Date
}

export type ExerciseTypes = "running" | "walking" | "cycling" | "swimming"

export type TrainingData = {
    endTime: string,
    metrics: {
        [key: string]: number
    },
    series: {
        [key: string]: number[]
    },
    startTime: string,
    exerciseType: ExerciseTypes
}