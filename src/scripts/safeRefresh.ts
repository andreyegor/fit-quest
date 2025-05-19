import { ApiRequests, ApiResponse } from "./ApiRequests.ts";

async function safeRefresh(): Promise<boolean> {
    const response: ApiResponse = await ApiRequests.refresh()
    if (response.status === "fail") {
        sessionStorage.setItem("redirectURL", window.location.href)
        window.location.href = "/login"
        return false
    } return true
}

export default safeRefresh()