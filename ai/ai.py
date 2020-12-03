from flask import Flask, request, jsonify
from fastai.learner import load_learner
from fastai.vision.core import PILImage
import sys

app = Flask(__name__)

# put pkls on the commandline
ai_scorers = []
for ai_path in sys.argv[1:]:
    print(f"Loading {ai_path}")
    ai_scorers.append(load_learner(ai_path))

@app.route('/ai/v1/score', methods=['GET'])
def get_score():
    if 'file' in request.args:
        file = request.args['file']
    else:
        return "Error: No file field provided. Please specify a file."
    img = PILImage.create(file)
    # predict returns ('no', tensor(0), tensor([1.0000e+00, 7.8869e-10]))
    no_score = 0.0
    yes_score = 0.0
    for ai in ai_scorers:
        score = ai.predict(img)
        #prediction = score[0]
        no_score += score[2][0].item()
        print(score[2][1].item())
        yes_score += score[2][1].item()
    # catching AttributeError: 'TypeDispatch' object has no attribute 'owner'
    #score = learn_inf.predict(file)[0]
    return jsonify({'file': file, 'no': no_score, 'yes': yes_score})

if __name__ == '__main__':
    app.run(host="localhost", port=8484, debug=True)
