class PasswordEyeToggle {
    private selectors = {
        root: '.input[type=password]',
        wrapper: '.input__wrapper',
        icon: '.input__icon--password'
    }
    private URLs = {
        openedEye: 'var(--openedURL)',
        closedEye: 'var(--closedURL)'
    }

    constructor() {
        this.bindEvents()
    }

    private bindEvents(): void {
        document.querySelectorAll(this.selectors.wrapper).forEach(wrapperElement => {
            const rootElement: HTMLInputElement | null = wrapperElement.querySelector(this.selectors.root)
            const iconElement: HTMLElement | null = wrapperElement.querySelector(this.selectors.icon)
            if (iconElement && rootElement) {
                rootElement.addEventListener('input', () => this.onInputChange(rootElement, iconElement))
                iconElement.addEventListener('click', () => this.onIconClick(rootElement, iconElement))
            }
        })
    }

    private onInputChange(rootElement: HTMLInputElement, iconElement: HTMLElement): void {
        const value: string = rootElement.value
        if (!iconElement.style.display && value) {
            iconElement.style.display = 'block'
        } else if (iconElement.style.display && !value) {
            iconElement.removeAttribute('style')
        }
    }

    private onIconClick(rootElement: HTMLInputElement, iconElement: HTMLElement): void {
        if (rootElement.type === "password") {
            rootElement.type = "text"
            iconElement.style.backgroundImage = this.URLs.closedEye
        } else {
            rootElement.type = "password"
            iconElement.style.backgroundImage = this.URLs.openedEye
        }
    }
}

export default PasswordEyeToggle