import {ApiResponse, RegisterCredentials, LoginCredentials, UserInfo, TrainingData} from "./types.ts";
import safeRefresh from "./safeRefresh.ts";

export class ApiRequests {
    private static roads = {
        // register: "/api/users",
        register: "https://fit-quest.ru/api/users",
        // login: "/api/auth/login",
        login: "https://fit-quest.ru/api/auth/login",
        // logout: "/api/auth/logout",
        logout: "https://fit-quest.ru/api/auth/logout",
        // refresh: "/api/auth/refresh",
        refresh: "https://fit-quest.ru/api/auth/refresh",
        // status: "/api/auth/status",
        status: "https://fit-quest.ru/api/auth/status",
        // exercises: "/api/exercises"
        exercises: "https://fit-quest.ru/api/exercises"
    }

    public static register = async (creds: RegisterCredentials): Promise<ApiResponse> => {
        const {email, username, password} = creds

        try {
            const response: Response = await fetch(this.roads.register,
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
                console.error(`[Log] Error ${response.status} ${message ? `: ${message}` : ""}`)
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
            const response: Response = await fetch(this.roads.login,
                {
                    method: "POST",
                    credentials: "include",
                    body: JSON.stringify(creds)
                })
            if (response.ok) {
                const userInfo = await this.getUserInfo()
                if (userInfo) {
                    sessionStorage.setItem("username", userInfo.name)
                    sessionStorage.setItem("userEmail", userInfo.email)
                    sessionStorage.setItem("userId", userInfo.id)
                }

                return {
                    status: "ok",
                    message: "Вход успешно выполнен"
                }
            } else if (response.status === 400) {
                return {
                    status: "fail",
                    message: "Почта или пароль введены неверно"
                }
            } else {
                const message: string = await response.text()
                console.error(`[Log] Error ${response.status} ${message ? `: ${message}` : ""}`)
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
            const response: Response = await fetch(this.roads.refresh,
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
                console.error(`[Log] Error ${response.status} ${message ? `: ${message}` : ""}`)
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

    public static async logout(): Promise<ApiResponse> {
        try {
            const response: Response = await fetch(this.roads.logout,
                {
                    method: "POST",
                    credentials: "include",
                })

            if (response.ok) {
                localStorage.removeItem("username")
                localStorage.removeItem("userEmail")
                localStorage.removeItem("userId")
                return {
                    status: "ok",
                    message: "Выход успешно выполнен"
                }
            } else {
                const message: string = await response.text()
                console.error(`[Log] Error ${response.status} ${message ? `: ${message}` : ""}`)
                return {
                    status: "fail",
                    message: "Что-то пошло не так..."
                }
            }
        } catch (e: any) {
            console.error(`[Log] Network error${e.message ? `: ${e.message}` : ''}`)
            return {
                status: "fail",
                message: "Произошла сетевая ошибка"
            }
        }
    }

    public static async getUserInfo(): Promise<UserInfo | null> {
        try {
            const response = await fetch(this.roads.status, {method: "GET", credentials: "include"})
            if (response.ok) {
                const userInfo: UserInfo = await response.json()
                localStorage.setItem("username", userInfo.name)
                localStorage.setItem("userEmail", userInfo.email)
                localStorage.setItem("userId", userInfo.id)
                return userInfo
            } else {
                localStorage.removeItem("username")
                localStorage.removeItem("userEmail")
                localStorage.removeItem("userId")
                const message: string = await response.text()
                console.error(`[Log] Error ${response.status} ${message ? `: ${message}` : ""}`)
                return null
            }
        } catch (e: any) {
            console.error(`[Log] Network error${e.message ? `: ${e.message}` : ''}`)
            return null
        }
    }

    public static async getTrainingData(params?: {offset?: number, limit?: number, types?: number[], timePeriod: Date[]}): Promise<TrainingData[] | null> {
        try {
            let fetchURL = this.roads.exercises
            if (params) {
                fetchURL += "?"
                if (params.offset) {
                    fetchURL += `${fetchURL.at(-1) === "?" ? "" : "&"}offset=${params.offset}`
                } if (params.limit) {
                    fetchURL += `${fetchURL.at(-1) === "?" ? "" : "&"}limit=${params.limit}`
                } if (params.types && params.types.length !== 0) {
                    fetchURL += `${fetchURL.at(-1) === "?" ? "" : "&"}types=${params.types.join(",")}`
                }
                // if (params.timePeriod && params.timePeriod.length === 2) {
                //     fetchURL += `${fetchURL.at(-1) === "?" ? "" : "&"}start=${params.timePeriod[0]}&end=${params.timePeriod[1]}`
                // }
            }
            const response: Response = await fetch(fetchURL, {
                method: "GET",
                credentials: "include"
            })

            if (response.ok || (response.status === 401 && await safeRefresh())) {
                return await response.json() as TrainingData[]
            } else {
                const message: string = await response.text()
                console.error(`[ApiRequests] ${response.status}: ${message ? `: ${message}` : ""}`)
                return null
            }
        } catch (e: any) {
            console.error(`[ApiRequests] Network error: ${e.message ? `: ${e.message}` : ''}`)
            return null
        }
    }
}

export default ApiRequests