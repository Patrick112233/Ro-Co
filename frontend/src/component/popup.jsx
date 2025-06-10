import React from 'react';

/**
 * General custom Popup
 * @param props props.trigger to enable the popup
 * @returns {React.JSX.Element|null}
 * @constructor
 */
function Popup(props) {
    return (props.trigger) ? (
        <div className="popup">
            <div className="popup-inner justify-content-between">
                    {props.children}
            </div>
        </div>
    ) : null;
}

export default Popup;