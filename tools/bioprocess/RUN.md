# Bioprocess English: daily lesson run

1. `pip install --break-system-packages -q kokoro-onnx` (ffmpeg must be available).
2. Lesson number = days since 2026-11-19 + 2 (2026-11-20 is lesson 03).
   Curriculum entry = curriculum.json[(number - 3) % 30]. From the second cycle on, use the same topic with a completely new scenario, new reading, dialogue and vocabulary.
3. Read lessons/lesson_02.json: it is the format and quality reference. Write lessons/lesson_NN.json with the same keys.
4. `python3 build_lesson.py lessons/lesson_NN.json Bioprocess_English_Lesson_NN.html` (audio is generated; takes about 10 minutes).
5. Options are shuffled by the builder, so it's fine to write every correct answer at index 0.
