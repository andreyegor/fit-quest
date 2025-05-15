import contentHeightSetter from "./ContentHeightSetter.ts";

class FormValidator {
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

    constructor(patterns={}, submitMethod=() => {}) {
        this.patterns = patterns
        this.submitMethod = submitMethod
        this.bindEvents()
    }

    manageErrors(fieldControlElement, errorMessages) {
        const fieldErrorsElement = fieldControlElement.parentElement.parentElement.querySelector(this.selectors.infoField)

        if (errorMessages.length > 0) {
            fieldErrorsElement.classList.add("error")
            fieldControlElement.classList.add("error")
            contentHeightSetter.setHeight(fieldErrorsElement, errorMessages.join("<br>"))
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

    onSubmit(event) {
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
            event.preventDefault()
            firstInvalidFieldControl.focus()
        } else {
            this.submitMethod()
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

export default FormValidator