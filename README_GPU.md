# GPU Integration

The application integrates with the online GPU using FastAPI.
1. The app takes a saved profile photo and an outfit image.
2. It uploads both files to the `VtonApiService`'s ngrok endpoint.
3. The endpoint runs an IDM-VTON pipeline and returns the result image URL.
