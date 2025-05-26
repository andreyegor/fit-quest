function setContentHeight(rootElement: HTMLElement, innerHTML: string): void {
    const prevHeight: number = rootElement.scrollHeight;

    rootElement.style.height = `${prevHeight}px`;
    rootElement.style.overflow = 'hidden'

    rootElement.innerHTML = innerHTML
    void rootElement.offsetHeight

    const nextHeight: number = rootElement.scrollHeight;
    rootElement.style.height = `calc(${nextHeight}px + var(--padding) * 2)`;
}

export default setContentHeight