import argparse
import os
import re
import time

import torch
import pandas as pd
from kernel_utils import VideoReader, FaceExtractor, confident_strategy, predict_on_video_set
from training.zoo.classifiers import DeepFakeClassifier
#arg('--models', nargs='+', required=True, help="checkpoint files")
#arg('--models', default="final_999_DeepFakeClassifier_tf_efficientnet_b7_ns_0_23", help="checkpoint files")
def predict():
    parser = argparse.ArgumentParser("Predict test videos")
    arg = parser.add_argument
    arg('--weights-dir', type=str, default="E:\\android_project\\weights", help="path to directory with checkpoints")
    #arg('--models', nargs='+', required=True, help="checkpoint files")
    arg('--test-dir', default='E:\\android_project\\dfdc_deepfake_challenge\\data', help="path to directory with videos")
    arg('--output', default='E:\\android_project\\output\\real_data_present_detection.csv', help="path to output csv")
    args = parser.parse_args()

    model_list = ['final_111_DeepFakeClassifier_tf_efficientnet_b7_ns_0_36', 'final_555_DeepFakeClassifier_tf_efficientnet_b7_ns_0_19', 'final_777_DeepFakeClassifier_tf_efficientnet_b7_ns_0_29', 'final_777_DeepFakeClassifier_tf_efficientnet_b7_ns_0_31', 'final_888_DeepFakeClassifier_tf_efficientnet_b7_ns_0_37', 'final_888_DeepFakeClassifier_tf_efficientnet_b7_ns_0_40', 'final_999_DeepFakeClassifier_tf_efficientnet_b7_ns_0_23']
    models = []
    model_paths = [os.path.join(args.weights_dir, model) for model in model_list]
    for path in model_paths:
        model = DeepFakeClassifier(encoder="tf_efficientnet_b7_ns").to("cuda")
        print("loading state dict {}".format(path))
        checkpoint = torch.load(path, map_location="cpu")
        state_dict = checkpoint.get("state_dict", checkpoint)
        model.load_state_dict({re.sub("^module.", "", k): v for k, v in state_dict.items()}, strict=False)
        model.eval()
        del checkpoint
        models.append(model.half())

    frames_per_video = 32
    video_reader = VideoReader()
    video_read_fn = lambda x: video_reader.read_frames(x, num_frames=frames_per_video)
    face_extractor = FaceExtractor(video_read_fn)
    input_size = 380
    strategy = confident_strategy
    stime = time.time()

    test_videos = sorted([x for x in os.listdir(args.test_dir) if x[-4:] == ".mp4"])
    predictions = predict_on_video_set(face_extractor=face_extractor, input_size=input_size, models=models,
                                       strategy=strategy, frames_per_video=frames_per_video, videos=test_videos,
                                       num_workers=1, test_dir=args.test_dir)
    return predictions