import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faPencil} from "@fortawesome/free-solid-svg-icons";
import React from "react";


const NewBtn = () => {
    return (
        <>
            <div className="dropstart position-fixed bottom-0 end-0" style={{zIndex: 100, marginRight: '5VW', marginBottom: '70px'}}>
                <button type="button" className="rounded-circle btn btn-primary" id="dropdownMenuButton"
                        data-bs-toggle="dropdown" aria-expanded="false">
                    <FontAwesomeIcon icon={faPencil} className="text-white p-2 fs-4"/>
                </button>
                <ul className="dropdown-menu" aria-labelledby="dropdownMenuButton">
                    <li><a className="dropdown-item" href="#">Ask Question</a></li>
                    <li><a className="dropdown-item" href="#">Create Event</a></li>
                </ul>
            </div>
        </>
    );
}

export default NewBtn;