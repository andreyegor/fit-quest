import dropmenuToggle from "../scripts/dropmenuToggle.ts";
import Trainings from "../scripts/Trainings.ts";
import LogoutButton from "../scripts/LogoutButton.ts";
import ApiRequests from "../scripts/ApiRequests.ts";

const userNameFieldElement = document.querySelector(".user-button__name")
const userName = sessionStorage.getItem("username")
if (userNameFieldElement) {
    if (!userName) {
        ApiRequests.getUserInfo().then(userInfo => {
            if (userInfo) {
                sessionStorage.setItem("username", userInfo.name)
                sessionStorage.setItem("userEmail", userInfo.email)
                sessionStorage.setItem("userId", userInfo.id)
            }
        })
    }
    userNameFieldElement.textContent = userName
}

dropmenuToggle()
new Trainings
new LogoutButton()
