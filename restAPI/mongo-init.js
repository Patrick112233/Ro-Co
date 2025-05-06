var username   = process.env.MONGO_INITDB_ROOT_USERNAME;
var pwd   = process.env.MONGO_INITDB_ROOT_PASSWORD;


db = db.getSiblingDB("RoCoDB");
//db.createCollection("dummyCollection"); // Dummy collection to create the DB

//print("Started Adding the Users.");
db.createUser({
    user: username,
    pwd: pwd,
    roles: [{ role: "readWrite", db: "RoCoDB" }],
});
db = db.getSiblingDB("admin");
/*
db.createUser({
    user: "admin",
    pwd: "root",
    roles: [{ role: "readWrite", db: "admin" }],
});*/
print("End Adding the User Roles.")
