function dropmenuToggle(): void {
    function openDropmenu(element: HTMLElement): void {
        element.classList.add("is-active")

        const optionsElements = element.querySelector(selectors.menu) as HTMLElement | null
        if (optionsElements) {
            const computedStyle = getComputedStyle(optionsElements);
            const borderTop = parseFloat(computedStyle.borderTopWidth);
            const borderBottom = parseFloat(computedStyle.borderBottomWidth);

            optionsElements.style.height = optionsElements.scrollHeight + borderBottom + borderTop + "px"
        }
    }

    function closeDropmenu(element: HTMLElement): void {
        setTimeout(() => {
            element.classList.remove("is-active")
        }, 200)

        const optionsElements = element.querySelector(selectors.menu) as HTMLElement | null
        if (optionsElements) {
            optionsElements.style.removeProperty("height")
        }
    }

    const selectors = {
        menuOpenButton: ".dropmenu",
        menu: ".dropmenu__options",
        activeMenu: ".dropmenu.is-active",
        option: ".dropmenu__option"
    }

    document.addEventListener('click', (event: MouseEvent) => {
        const target = event.target as HTMLElement

        const activeElement = document.querySelector(selectors.activeMenu) as HTMLElement | null
        if (!activeElement) {
            const dropmenuElement = target.closest(selectors.menuOpenButton) as HTMLElement | null
            if (dropmenuElement) {
                openDropmenu(dropmenuElement)
            }
        } else {
            if (target.closest(selectors.menuOpenButton)) {
                closeDropmenu(activeElement)
            } else {
                const newActiveElement = target.closest(selectors.menuOpenButton) as HTMLElement | null
                if (newActiveElement != activeElement) {
                    closeDropmenu(activeElement)

                    if (newActiveElement) {
                        openDropmenu(newActiveElement)
                    }
                }
            }
        }
    })
}

export default dropmenuToggle