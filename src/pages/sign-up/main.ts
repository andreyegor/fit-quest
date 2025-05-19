import AbsolutePos from "../../scripts/AbsolutePos.ts";
import PasswordEyeToggle from "../../scripts/PasswordEyeToggle.ts";
import FormValidate from "../../scripts/FormValidate.js";
import ApiRequests from "../../scripts/ApiRequests.ts";

new AbsolutePos(".footer__background", "top", 175)
new PasswordEyeToggle()

new FormValidate(ApiRequests.register as () => Promise<any>, sessionStorage.getItem("redirectURL") || "/feed", {
    "#inputPassword": /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$/
})