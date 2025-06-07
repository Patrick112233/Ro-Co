import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faChevronDown, faChevronUp} from "@fortawesome/free-solid-svg-icons";
import React, {useState} from "react";
import {motion} from "motion/react";


const Question = () => {

    const [isOpen, setIsOpen] = useState(false);


    return (<>
    <div className="px-2 py-1">
        <div className="card mb-2">
            <motion.div className="card-body p-2 p-sm-3 ">
                <div className="d-flex align-items-center">
                    <div className="d-flex align-items-center">
                        <a href="#"><img
                            src="https://bootdey.com/img/Content/avatar/avatar1.png"
                            className="mr-3 rounded-circle"
                            width="50" height="50" alt="User Name"/></a>
                        <div className="d-flex flex-column align-items-bottom ps-3">
                            <p className="mb-0">
                                <a href="/profile">User name</a> Asked
                                <span className="text-secondary font-weight-bold">13 minutes ago</span>
                            </p>
                            <h4 className="pt-1 mb-1">Realtime fetching data</h4>
                            <div className="d-flex flex-wrap mb-1">
                                            <span className="badge me-2"
                                                  style={{
                                                      backgroundColor: '#1735CEFF', color: '#fff'
                                                  }}>Informatics</span>
                                <span className="badge me-2"
                                      style={{backgroundColor: '#BF6E06FF', color: '#fff'}}>Database</span>
                                <span className="badge me-2"
                                      style={{backgroundColor: '#095705FF', color: '#fff'}}>Real-Time</span>
                            </div>
                        </div>
                    </div>
                </div>
                <div>
                    <p className="text-secondary mb-2">
                        I'm a beginner with Laravel and I'm trying to fetch data from the database in
                        real-time for my dashboard analytics. I attempted a solution using AJAX, but it
                        didn't work as expected. If anyone has a straightforward and effective approach,
                        it would be greatly appreciated.
                        Additionally, I'm looking for a method that integrates seamlessly with Laravel's
                        ecosystem, ensuring minimal performance overhead. Any guidance or examples would
                        be incredibly helpful for someone new to this framework.
                    </p>
                </div>
                <div className="d-flex justify-content-center">
                    <button className="btn btn-link p-0" onClick={() => setIsOpen(!isOpen)}>
                        {isOpen ? (<FontAwesomeIcon icon={faChevronUp}/>) : (<FontAwesomeIcon icon={faChevronDown}/>)}
                    </button>
                </div>
            </motion.div> {isOpen && (
                <motion.div>
                    <div className="card-footer py-3 border-0">
                        {/*Answare*/}
                        <div className="card-body p-3 bg-light border border-dark-subtle rounded">
                            <div className="d-flex flex-start ">
                                <img className="rounded-circle shadow-1-strong me-3"
                                     src="https://mdbcdn.b-cdn.net/img/Photos/Avatars/img%20(26).webp" alt="avatar"
                                     width="50"
                                     height="50"/>
                                <div>
                                    <h6 className="fw-bold mb-1">Lara Stewart</h6>
                                    <div className="d-flex align-items-center mb-3">
                                        <p className="mb-0">
                                            March 15, 2021
                                        </p>s
                                    </div>
                                    <p className="mb-0">
                                        Contrary to popular belief, Lorem Ipsum is not simply random text. It
                                        has roots in a piece of classical Latin literature from 45 BC, making it
                                        over 2000 years old. Richard McClintock, a Latin professor at
                                        Hampden-Sydney College in Virginia, looked up one of the more obscure
                                        Latin words, consectetur, from a Lorem Ipsum passage, and going through
                                        the cites.
                                    </p>
                                </div>
                            </div>
                        </div>
                        {/*/Answare*/}
                        {/*Post*/}
                        <div className="d-flex flex-start w-100 pt-3 ">
                            <div className="form-outline w-100 ">
                                        <textarea className="form-control border-dark-subtle " id="textAreaExample" rows="4"
                                                  style={{background: '#fff'}}></textarea>
                            </div>
                        </div>
                        <div className=" mt-2 pt-1 d-flex justify-content-end">
                            <button type="button"
                                    className="me-2 btn btn-primary btn-sm text-white">Post comment
                            </button>
                            <button type="button"
                                    className="me-2 btn btn-outline-primary btn-sm">Cancel
                            </button>
                        </div>
                    </div>
                </motion.div>
                )}
        </div>
    </div>
</>);
}

export default Question;