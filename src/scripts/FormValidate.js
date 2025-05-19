import setContentHeight from "./setContentHeight.ts";

class FormValidate {
    selectors = {
        form: "[data-js-form]",
        infoField: "[data-js-form-infos]",
        againFieldPart: "Again"
    }

    errorMessages = {
        patternMismatch: ({title}) => title || "Введённое значение не соответствуют нужному формату",
        tooLong: ({maxLength}) => `Слишком длинное значение, максимум символов - ${maxLength}`,
        tooShort: ({minLength}) => `Слишком коротко, минимум символов - ${minLength}`,
        valueMissing: (element) => "Заполните это поле" + (element.minLength > 0 ? `. Минимум символов - ${element.minLength}` : "")
    }

    /**
     * @param {string} redirectURL
     * @param {Object.<string, RegExp>} patterns
     * @param {() => Promise} submitMethod
     */
    constructor(submitMethod=() => {}, redirectURL="", patterns={}) {
        this.submitMethod = submitMethod
        this.redirectURL = redirectURL
        this.patterns = patterns
        this.bindEvents()
    }

    manageErrors(fieldControlElement, errorMessages) {
        const fieldErrorsElement = fieldControlElement.parentElement.parentElement.querySelector(this.selectors.infoField)

        if (errorMessages.length > 0) {
            fieldErrorsElement.classList.add("error")
            fieldControlElement.classList.add("error")
            setContentHeight(fieldErrorsElement, errorMessages.join("<br>"))
        }
        else {
            fieldErrorsElement.classList.remove('error')
            fieldControlElement.classList.remove("error")
        }
    }

    validateField(fieldControlElement) {
        const errors = fieldControlElement.validity
        const errorMessages = []

        const elementId = fieldControlElement.id
        if (elementId.indexOf(this.selectors.againFieldPart) !== -1) {
            const targetControlElement = document.getElementById(elementId.slice(0, elementId.indexOf(this.selectors.againFieldPart)))

            if (fieldControlElement.value !== targetControlElement.value || !targetControlElement.value) {
                errorMessages.push(this.errorMessages.patternMismatch(fieldControlElement))
            }
        }

        Object.entries(this.patterns).forEach(([fieldControlSelector, pattern]) => {
            if (fieldControlElement.matches(fieldControlSelector) && !fieldControlElement.value.match(pattern)) {
                errorMessages.push(this.errorMessages.patternMismatch(fieldControlElement))
            }
        })
        Object.entries(this.errorMessages).forEach(([errorType, getErrorMessage]) => {
            if (fieldControlElement.id.indexOf(this.selectors.againFieldPart) === -1 && errors[errorType]) {
                errorMessages.push(getErrorMessage(fieldControlElement))
            }
        })

        this.manageErrors(fieldControlElement, errorMessages)

        return errorMessages.length === 0
    }

    onBlur(event) {
        const { target } = event
        const isFormField = target.closest(this.selectors.form)
        const isRequired = target.required

        if (isFormField && isRequired) {
            this.validateField(target)
        }
    }

    onChange(event) {
        const { target } = event
        const isFormField = target.closest(this.selectors.form)
        const isRequired = target.required
        const isToggleType = ["radio", "checkbox"].includes(target.type)

        if (isRequired && isToggleType && isFormField) {
            this.validateField(target)
        }
    }

    async onSubmit(event) {
        event.preventDefault()

        const { target } = event
        const isFormElement = target.matches(this.selectors.form)
        if (!isFormElement) { return }

        const requiredControlElements = [...target.elements].filter(({ required }) => required)

        let isFormValid = true
        let firstInvalidFieldControl = null
        requiredControlElements.forEach(requiredControlElement => {
            if (!this.validateField(requiredControlElement)) {
                isFormValid = false

                if (!firstInvalidFieldControl) {
                    firstInvalidFieldControl = requiredControlElement
                }
            }
        })

        if (!isFormValid) {
            firstInvalidFieldControl.focus()
        } else {
            const formData = new FormData(document.querySelector(this.selectors.form))
            const creds = Object.fromEntries(formData)

            const response = await this.submitMethod(creds)
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
}

export default FormValidate