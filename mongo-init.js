db = db.getSiblingDB("RoCoDB");
//db.createCollection("dummyCollection"); // Dummy collection to create the DB

//print("Started Adding the Users.");
db.createUser({
    user: "backend",
    pwd: "fnwMSjCDyc",
    roles: [{ role: "readWrite", db: "RoCoDB" }],
});
db = db.getSiblingDB("admin");
db.createUser({
    user: "admin",
    pwd: "root",
    roles: [{ role: "readWrite", db: "admin" }],
});
print("End Adding the User Roles.")
