var username = process.env.MONGO_INITDB_ROOT_USERNAME;
var pwd = process.env.MONGO_INITDB_ROOT_PASSWORD;

db = db.getSiblingDB("RoCoDB");
db.createUser({
    user: username,
    pwd: pwd,
    roles: [{ role: "readWrite", db: "RoCoDB" }]
});
print("User created in RoCoDB.");