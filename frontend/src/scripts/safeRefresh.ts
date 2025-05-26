import ApiRequests from "./ApiRequests.ts"
import {ApiResponse} from "./types.ts";

async function safeRefresh(): Promise<boolean> {
    const response: ApiResponse = await ApiRequests.refresh()
    if (response.status === "fail") {
        sessionStorage.setItem("redirectURL", window.location.href)
        window.location.href = "/sign-in/"
        return false
    } return true
}

export default safeRefresh