import { ApiResponse, RegisterCredentials, LoginCredentials } from "./types.ts";

export class ApiRequests {
    public static register = async (creds: RegisterCredentials): Promise<ApiResponse> => {
        const { email, username, password } = creds

        try {
            const response: Response = await fetch("/api/users",
                {
                    method: "POST",
                    credentials: "include",
                    body: JSON.stringify({
                        name: username,
                        email: email,
                        password: password
                    })
                }
            )
            if (response.ok) {
                await this.login({
                    email: email,
                    password: password
                })
                return {
                    status: "ok",
                    message: "Регистрация прошла успешно",
                }
            }
            if (response.status === 409) {
                return {
                    status: "fail",
                    message: "Пользователь с такой почтой уже существует"
                }
            } else {
                const message: string = await response.text()
                console.error(`[Log] Error ${response.status} ${ message ? `: ${message}` : ""}`)
                return {
                    status: "fail",
                    message: "Что-то пошло не так..."
                }
            }
        } catch (e: any) {
            console.error(`[Log] Network error${e.message ? ` : ${e.message}` : ''}`)
            return {
                status: "fail",
                message: "Произошла сетевая ошибка"
            }
        }
    }

    public static login = async (creds: LoginCredentials): Promise<ApiResponse> => {
        try {
            const response: Response = await fetch("/api/auth/login",
                {
                    method: "POST",
                    credentials: "include",
                    body: JSON.stringify(creds)
                })
            if (response.ok) {
                await this.refresh()
                return {
                    status: "ok",
                    message: "Вход успешно выполнен"
                }
            } else if (response.status === 400) {
                return {
                    status: "fail",
                    message: "Почта или пароль введены неверно"
                }
            }
            else {
                const message: string = await response.text()
                console.error(`[Log] Error ${response.status} ${ message ? `: ${message}` : ""}`)
                return {
                    status: "fail",
                    message: "Что-то пошло не так..."
                }
            }
        } catch (e: any) {
            console.error(`[Log] Network error${e.message ? ` : ${e.message}` : ''}`)
            return {
                status: "fail",
                message: "Произошла сетевая ошибка"
            }
        }
    }

    public static async refresh(): Promise<ApiResponse> {
        try {
            const response: Response = await fetch("/api/auth/refresh",
                {
                    method: "POST",
                    credentials: "include"
                })
            if (response.ok) {
                return {
                    status: "ok",
                    message: "Refreshed token successfully"
                }
            } else if (response.status === 400) {
                const message: string = await response.text()
                return {
                    status: "fail",
                    message: message
                }
            } else {
                const message: string = await response.text()
                console.error(`[Log] Error ${response.status} ${ message ? `: ${message}` : ""}`)
                return {
                    status: "fail",
                    message: `Error ${response.status} ${message ? `: ${message}` : ""}`
                }
            }
        } catch (e: any) {
            console.error(`[Log] Network error${e.message ? ` : ${e.message}` : ''}`)
            return {
                status: "ok",
                message: `Network error${e.message ? ` : ${e.message}` : ''}`
            }
        }
    }
}

export default ApiRequests