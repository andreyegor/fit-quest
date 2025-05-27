import AbsolutePos from "../scripts/AbsolutePos.ts";
import PasswordEyeToggle from "../scripts/PasswordEyeToggle.ts";
import FormValidate from "../scripts/FormValidate.ts";
import ApiRequests from "../scripts/ApiRequests.ts";
import dropmenuToggle from "../scripts/dropmenuToggle.ts";
import LogoutButton from "../scripts/LogoutButton.ts";

ApiRequests.getUserInfo().then(userInfo => {
    if (userInfo) {
        window.location.href = "/trainings/"
    }
})

dropmenuToggle()
new AbsolutePos(".footer__background", "top", 175)
new PasswordEyeToggle()
new FormValidate(ApiRequests.register as () => Promise<any>, "/sign-in/", {
    "#inputPassword": /^(?=.*[A-Za-z])(?=.*\d).{8,}$/
})
new LogoutButton()
