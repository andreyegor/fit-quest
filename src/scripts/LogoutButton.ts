import ApiRequests from "./ApiRequests.ts";

class LogoutButton {
    private readonly selectors = {
        root: "#logout-button"
    }

    public constructor() {
        this.bindEvents()
    }

    private bindEvents() {
        document.addEventListener("click", (event: MouseEvent) => {
            this.onClick(event)
        })
    }

    private onClick(event: MouseEvent) {
        const target = event.target as HTMLElement | null
        if (target && target.matches(this.selectors.root)) {
            ApiRequests.logout()
            window.location.href = "/src/pages/sign-in/index.html"
        }
    }
}

export default LogoutButton