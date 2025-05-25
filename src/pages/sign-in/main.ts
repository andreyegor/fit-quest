import AbsolutePos from "../../scripts/AbsolutePos.ts";
import PasswordEyeToggle from "../../scripts/PasswordEyeToggle.ts";
import FormValidate from "../../scripts/FormValidate.ts";
import ApiRequests from "../../scripts/ApiRequests.ts";
import dropmenuToggle from "../../scripts/dropmenuToggle.ts";

dropmenuToggle()
new AbsolutePos(".footer__background", "top", 175)
new PasswordEyeToggle()
new FormValidate(ApiRequests.login as () => Promise<any>, sessionStorage.getItem("redirectURL") || "/feed")
