from flask import Flask, request, jsonify
from fastai.learner import load_learner
from fastai.vision.core import PILImage
import sys

app = Flask(__name__)

# this needs to match the function from training
def get_y_from_first_int(x):
    return list(range(int(x.name.split("__")[0])+1))

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
    # predict returns 
    # 0 = prediction, 1 = prediction, 2 = prediction score
    # Yes/No: ('no', 
    #          tensor(0), 
    #          tensor([1.0000e+00, 7.8869e-10]))
    # Stars: ([0, 1], 
    #         tensor([ True,  True, False, False, False]), 
    #         tensor([0.9800, 0.8896, 0.0067, 0.0070, 0.0064]))
    score = 0.0
    for ai in ai_scorers:
        prediction = ai.predict(img)
        prediction_tensor = prediction[2]
        print(f"{file} -> {prediction_tensor}")
        sub_score = 0.0
        for i,v in enumerate(prediction_tensor):
            if i == 0:
                sub_score += -1 * v.item()
            else:
                sub_score += v.item()
        score += sub_score
    return jsonify({'file': file, 'score': score})

if __name__ == '__main__':
    app.run(host="localhost", port=8484, debug=True)
