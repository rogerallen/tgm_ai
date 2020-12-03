from flask import Flask, request, jsonify
from fastai.learner import load_learner
from fastai.vision.core import PILImage

app = Flask(__name__)

# fixme commandline option for pkl
learn_inf = load_learner('./ai/export_201130_manual.pkl')

@app.route('/ai/v1/score', methods=['GET'])
def get_score():
    if 'file' in request.args:
        file = request.args['file']
    else:
        return "Error: No file field provided. Please specify a file."
    img = PILImage.create(file)
    # predict returns ('no', tensor(0), tensor([1.0000e+00, 7.8869e-10]))
    score = learn_inf.predict(img)
    prediction = score[0]
    no_score = score[2][0].item()
    yes_score = score[2][1].item()
    # catching AttributeError: 'TypeDispatch' object has no attribute 'owner'
    #score = learn_inf.predict(file)[0]
    return jsonify({'file': file, 'prediction': prediction, 'no': no_score, 'yes': yes_score})

if __name__ == '__main__':
    app.run(host="localhost", port=8484, debug=True)
