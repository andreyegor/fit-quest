import AbsolutePlacer from "../../scripts/AbsolutePlacer.ts";
import PasswordInputToggler from "../../scripts/PasswordInputToggler.ts";
import FormValidator from "../../scripts/FormValidator.js";

new AbsolutePlacer(".footer__background", "top", 175)
new PasswordInputToggler()
new FormValidator({
    "#inputPassword": /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$/
})
