# Bioprocess English: daily lesson run

The lessons live in ONE private Claude artifact: https://claude.ai/artifact/HWNL7vWgDEaNV3Jtzhevs9
- `app/index.html` is the page (lesson viewer, audio, quizzes, writing correction with Claude, progress).
- Each lesson is published beside it as `lessons/lesson_NN.json`, `lessons/lesson_NN_audio.json`, `lessons/lesson_NN.mp3`, listed in `lessons/index.json`.
- Corrected writings are stored in the artifact database, collection `writings`, one document per lesson (`lesson-NN`, fields: lesson, title, grammar, text, feedback, level, rules, at).

Steps:
1. `pip install --break-system-packages -q kokoro-onnx` (ffmpeg needed).
2. Lesson number N = days since 2026-11-19 + 2 (2026-11-20 is lesson 03). Topic = curriculum.json[(N - 3) % 30]; from the second cycle on, same topic with a new scenario.
3. Write lessons/lesson_NN.json in the format of lessons/lesson_02.json (correct option at index 0; the builder shuffles).
4. Read the artifact (Artifact action "read"), then download its `lessons/index.json` (Artifact read with `path`) into OUT/lessons/index.json.
5. `python3 build_data.py lessons/lesson_NN.json OUT` (about 10 minutes of audio generation).
6. Publish: Artifact publish with `url`, `file_path` = app/index.html, and `files` = the three new lesson files + the updated lessons/index.json. Don't pass `capabilities` (they carry forward).
