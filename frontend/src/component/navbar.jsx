import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faBell, faComment, faCalendar, faUser} from "@fortawesome/free-solid-svg-icons";
import React from "react";


const CustomNavbar = () => {

    const mobileBtnCount = 3; // Anzahl der Buttons

    const buttonStyle = {
        width: `calc((100% / ${mobileBtnCount}) - 10px)`, // 10px = 5px Padding auf beiden Seiten
        padding: '5px',
        boxSizing: 'border-box',
    };

    return (
        <>
            {/* Desktop Navbar */}
            <nav className="navbar navbar-light bg-primary justify-content-between w-100">
                <div className="d-flex justify-content-start btn-group ms-2">
                    <button className="rounded-circle btn btn-light" type="button">
                        <FontAwesomeIcon icon={faUser} className="text-dark" />
                    </button>
                    <a className="navbar-brand text-white fw-bold ms-2">User Name</a>
                </div>
                    <div className="d-flex justify-content-end btn-group">
                        <button className="btn me-2" type="button">
                            <FontAwesomeIcon icon={faBell} className="text-white fs-4" />
                        </button>
                    </div>

            </nav>

            {/* Mobile Navbar */}
            <nav className="navbar navbar-light bg-primary justify-content-around w-100 fixed-bottom d-md-none">
                <button className="btn" type="button" style={buttonStyle}>
                    <FontAwesomeIcon icon={faUser} className="text-white fs-4"/>
                </button>
                <button className="btn" type="button" style={buttonStyle}>
                    <FontAwesomeIcon icon={faComment} className="text-white fs-4" />
                </button>
                <button className="btn" type="button" style={buttonStyle}>
                    <FontAwesomeIcon icon={faCalendar} className="text-white fs-4"/>
                </button>
            </nav>
        </>
    );
};

export default CustomNavbar;