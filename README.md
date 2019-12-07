# Project 4 Webserver implemented in Java

### Todo
- [x] listen for requests on port 80
- [x] every connection pushed to a separate worker thread, closed once response is sent
- [ ] parse HTTP request to understand what to do
- [ ] return 405 if not GET, we ignore all POST/PUT
- [ ] return requested content, if GET request
- [x] check if requested file exists
