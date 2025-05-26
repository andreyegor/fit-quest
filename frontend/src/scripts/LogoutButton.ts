import ApiRequests from "./ApiRequests.ts";

class LogoutButton {
    private readonly selectors = {
        root: "#logoutButton"
    }

    public constructor() {
        this.bindEvents()
    }

    private bindEvents() {
        document.addEventListener("click", (event: MouseEvent) => {
            this.onClick(event)
        })
    }

    private async onClick(event: MouseEvent) {
        const target = event.target as HTMLElement | null
        if (target && target.matches(this.selectors.root)) {
            const response = await ApiRequests.logout()
            if (response.status === "ok") {
                window.location.href = "/sign-in/"
            }
        }
    }
}

export default LogoutButton