import AbsolutePos from "../../scripts/AbsolutePos.ts";
import PasswordEyeToggle from "../../scripts/PasswordEyeToggle.ts";
import FormValidate from "../../scripts/FormValidate.ts";
import ApiRequests from "../../scripts/ApiRequests.ts";

new AbsolutePos(".footer__background", "top", 175)
new PasswordEyeToggle()

new FormValidate(ApiRequests.register as () => Promise<any>, "/login", {
    "#inputPassword": /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$/
})