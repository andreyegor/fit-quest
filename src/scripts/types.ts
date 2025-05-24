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