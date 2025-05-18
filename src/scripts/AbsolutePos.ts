type absolutePosition = "top" | "bottom"

class AbsolutePos {
    private offset: number
    private position: absolutePosition
    private rootElement: HTMLElement | null

    constructor(rootSelector: string, position: absolutePosition, offset: number) {
        this.offset = offset
        this.position = position
        this.rootElement = document.querySelector(rootSelector)

        this._bindEvents()
    }

    _bindEvents(): void {
        this._eventHandler();

        ['load', 'resize', 'scroll'].forEach(event => {
            window.addEventListener(event, () => this._eventHandler())
        })
    }

    _eventHandler(): void {
        if (!this.rootElement) {
            throw new Error("AbsolutePos: root element doesn't exist")
        }
        if (!getComputedStyle(this.rootElement).getPropertyValue('--height') && ["top", "bottom"].includes(this.position)) {
            console.warn("AbsolutePos: it seems you don't have a --height css-variable. It can cause problems")
        }
        const contentHeight: number = this._getContentHeight()

        switch (this.position) {
            case "top":
                this.rootElement.style.top = `max(${this.offset}px, calc(${
                    this._isVirtualKeyboardOpen() ? `${contentHeight}px` : `max(${contentHeight}px, 100vh)`
                } - var(--height))`
                break;
            case "bottom":
                this.rootElement.style.bottom = `max(${this.offset}px, calc(${
                    this._isVirtualKeyboardOpen() ? `${contentHeight}px` : `max(${contentHeight}px, 100vh)`
                } - var(--height))`
                break;
        }
    }

    _getContentHeight(): number {
        const body = document.body,
            html = document.documentElement;

        return Math.max(body.scrollHeight, body.offsetHeight,
            html.clientHeight, html.scrollHeight, html.offsetHeight)
    }

    private _isVirtualKeyboardOpen(): boolean {
        if (!window.visualViewport) {
            return false
        }
        const isViewportShrunk = window.visualViewport?.height < window.innerHeight * 0.7;

        const isInputFocused = document.activeElement?.tagName === 'INPUT' ||
            document.activeElement?.tagName === 'TEXTAREA';

        return isViewportShrunk || isInputFocused;
    };
}

export default AbsolutePos;