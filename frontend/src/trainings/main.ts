import dropmenuToggle from "../scripts/dropmenuToggle.ts";
import Trainings from "../scripts/Trainings.ts";
import LogoutButton from "../scripts/LogoutButton.ts";
import ApiRequests from "../scripts/ApiRequests.ts";

const userNameFieldElement = document.querySelector(".user-button__name")
let userName = localStorage.getItem("username")
if (userNameFieldElement) {
    if (!userName) {
        ApiRequests.getUserInfo()
        userName = localStorage.getItem("username")
    }
    userNameFieldElement.textContent = userName
}

dropmenuToggle()
new Trainings
new LogoutButton()
