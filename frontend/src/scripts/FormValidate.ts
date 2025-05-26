import setContentHeight from "./setContentHeight.ts";
import {ApiResponse, FormInputElement} from "./types.ts"

class FormValidate {
    private readonly submitMethod: (creds: {[key: string]: FormDataEntryValue}) => Promise<ApiResponse | void>
    private readonly redirectURL: string
    private readonly patterns: { [selector: string]: RegExp }

    selectors = {
        form: "[data-js-form]",
        infoField: "[data-js-form-infos]",
        againFieldPart: "Again"
    }

    errorMessages: Partial<Record<keyof ValidityState, (el: FormInputElement) => string>> = {
        patternMismatch: (element: FormInputElement) => element.title || "Введённое значение не соответствуют нужному формату",
        tooLong: (element: FormInputElement) => `Слишком длинное значение, максимум символов - ${element.maxLength}`,
        tooShort: (element: FormInputElement) => `Слишком коротко, минимум символов - ${element.minLength}`,
        valueMissing: (element: FormInputElement) => "Заполните это поле" + (element.minLength > 0 ? `. Минимум символов - ${element.minLength}` : "")
    }

    constructor(submitMethod = async () => {}, redirectURL = "", patterns = {}) {
        this.submitMethod = submitMethod
        this.redirectURL = redirectURL
        this.patterns = patterns
        this.bindEvents()
    }

    manageErrors(fieldControlElement: FormInputElement, errorMessages: string[]) {
        const fieldErrorsElement: HTMLElement | null | undefined = fieldControlElement.parentElement?.parentElement?.querySelector(this.selectors.infoField)
        if (!fieldErrorsElement) {
            console.warn(`field-error element not found for ${fieldControlElement}`)
            return
        }

        if (errorMessages.length > 0) {
            fieldErrorsElement.classList.add("error")
            fieldControlElement.classList.add("error")
            setContentHeight(fieldErrorsElement, errorMessages.join("<br>"))
        } else {
            fieldErrorsElement.classList.remove('error')
            fieldControlElement.classList.remove("error")
        }
    }

    validateField(fieldControlElement: FormInputElement) {
        const errors: ValidityState = fieldControlElement.validity
        const errorMessages: string[] = []

        const elementId: string = fieldControlElement.id
        const indexOfAgainFieldPart: number = elementId.indexOf(this.selectors.againFieldPart)
        if (indexOfAgainFieldPart !== -1) {
            const el: HTMLElement | null = document.getElementById(elementId.slice(0, indexOfAgainFieldPart))

            let targetControlElement: HTMLInputElement
            if (el instanceof HTMLInputElement) {
                targetControlElement = el
            } else {
                console.warn(`input element not found for ${fieldControlElement}`)
                return
            }

            if ((targetControlElement && fieldControlElement.value !== targetControlElement.value || !targetControlElement.value)
                && this.errorMessages.patternMismatch) {
                errorMessages.push(this.errorMessages.patternMismatch(fieldControlElement))
            }
        }

        Object.entries(this.patterns).forEach(([fieldControlSelector, pattern]) => {
            if (fieldControlElement.matches(fieldControlSelector) && !fieldControlElement.value.match(pattern)
                && this.errorMessages.patternMismatch) {
                errorMessages.push(this.errorMessages.patternMismatch(fieldControlElement))
            }
        })
        Object.entries(this.errorMessages).forEach(([errorType, getErrorMessage]) => {
            const key = errorType as keyof ValidityState
            if (fieldControlElement.id.indexOf(this.selectors.againFieldPart) === -1 && errors[key]) {
                errorMessages.push(getErrorMessage(fieldControlElement))
            }
        })

        this.manageErrors(fieldControlElement, errorMessages)

        return errorMessages.length === 0
    }

    onBlur(event: FocusEvent) {
        const target = this._getInputTarget(event)
        if (!target) {
            console.warn(`no input target element for `, event)
            return
        }
        const isFormField: HTMLFormElement | null = target.closest(this.selectors.form)
        const isRequired: boolean = target.required

        if (isFormField && isRequired) {
            this.validateField(target)
        }
    }

    onChange(event: Event) {
        const target = this._getInputTarget(event)
        if (!target) {
            console.warn(`no input target element for ${event}`)
            return
        }
        const isFormField: HTMLFormElement | null = target.closest(this.selectors.form)
        const isRequired: boolean = target.required
        const isToggleType: boolean = ["radio", "checkbox"].includes(target.type)

        if (isRequired && isToggleType && isFormField) {
            this.validateField(target)
        }
    }

    async onSubmit(event: SubmitEvent) {
        event.preventDefault()

        const el: EventTarget | null = event.target
        let target: HTMLFormElement
        if (el instanceof HTMLFormElement) {
            target = el
        } else {
            console.warn(`target form element not found for ${event}`)
            return
        }

        const isFormElement: boolean = target.matches(this.selectors.form)
        if (!isFormElement) {
            return
        }

        const requiredControlElements = [...target.elements].filter(
            (element): element is FormInputElement => {
                if (!(element instanceof Element)) return false;
                return (
                    (element instanceof HTMLInputElement && element.required) ||
                    (element instanceof HTMLTextAreaElement && element.required)
                );
            }
        );

        let isFormValid = true
        let firstInvalidFieldControl: FormInputElement | null = null
        requiredControlElements.forEach(requiredControlElement => {
            if (!this.validateField(requiredControlElement)) {
                isFormValid = false

                if (!firstInvalidFieldControl) {
                    firstInvalidFieldControl = requiredControlElement
                }
            }
        })

        if (!isFormValid && firstInvalidFieldControl) {
            (firstInvalidFieldControl as FormInputElement).focus()
            // (firstInvalidFieldControl as unknown as HTMLInputElement | HTMLTextAreaElement).focus();
        } else {
            const formElement: HTMLFormElement | null = document.querySelector(this.selectors.form)
            if (!formElement) {
                console.warn(`form element not found by selector ${this.selectors.form}`)
                return
            }
            const formData = new FormData(formElement)
            const creds = Object.fromEntries(formData)

            const response = await this.submitMethod(creds)
            if (!response) { return }
            const infoFieldElements = document.querySelectorAll(this.selectors.infoField)
            const infoFieldLastElement = infoFieldElements[infoFieldElements.length - 1]
            infoFieldLastElement.classList.remove("success", "error")

            if (response.status === "ok") {
                infoFieldLastElement.classList.add("success")
                if (this.redirectURL) {
                    window.location.replace(this.redirectURL)
                }
            } else {
                infoFieldLastElement.classList.add("error")
            }
            infoFieldLastElement.textContent = response.message
        }
    }

    bindEvents() {
        document.addEventListener("blur", event => {
            this.onBlur(event)
        }, {capture: true})
        document.addEventListener("change", event => {
            this.onChange(event)
        })
        document.addEventListener("submit", event => {
            this.onSubmit(event)
        })
    }

    _getInputTarget(event: Event): FormInputElement | null {
        const el: EventTarget | null = event.target
        if (el instanceof HTMLInputElement || el instanceof HTMLTextAreaElement) {
            return el as FormInputElement
        }
        return null
    }
}

export default FormValidate