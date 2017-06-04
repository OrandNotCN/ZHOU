package com.lacuz.ffmpeg;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class FFmpegUtils {

	private static final int DEFAULT_WIDTH = 480;//默认输出宽度
	private static final int DEFAULT_HEIGHT = 800;//默认输出高度

	/**
	 * 处理单个视频
	 *
	 * @param epVideo      需要处理的视频
	 * @param outputOption 输出选项配置
	 * @return
	 */
	public static void exec(VideoDeal epVideo, OutputOption outputOption, JniProgressListener onEditorListener) {
		boolean isFilter = false;
		ArrayList<VideoSpecialEffects> epDraws = epVideo.getEpDraws();
		//开始处理
		StringBuilder cmd = new StringBuilder("-y");
		if (epVideo.getVideoClip()) {
			cmd.append(" -ss ").append(epVideo.getClipStart()).append(" -t ").append(epVideo.getClipDuration());
		}
		cmd.append(" -i ").append(epVideo.getVideoPath());
		//添加图片或者动图
		if (epDraws.size() > 0) {
			for (int i = 0; i < epDraws.size(); i++) {
				if (epDraws.get(i).isAnimation()) {
					cmd.append(" -ignore_loop 0");
				}
				cmd.append(" -i ").append(epDraws.get(i).getPicPath());
			}
			cmd.append(" -filter_complex [0:0]").append(epVideo.getFilters() != null ? epVideo.getFilters() + "," : "")
					.append("scale=").append(outputOption.width == 0 ? "iw" : outputOption.width).append(":")
					.append(outputOption.height == 0 ? "ih" : outputOption.height)
					.append(outputOption.width == 0 ? "" : ",setdar=" + outputOption.getSar()).append("[outv0];");
			for (int i = 0; i < epDraws.size(); i++) {
				cmd.append("[").append(i + 1).append(":0]").append(epDraws.get(i).getPicFilter()).append("scale=").append(epDraws.get(i).getPicWidth()).append(":")
						.append(epDraws.get(i).getPicHeight()).append("[outv").append(i + 1).append("];");
			}
			for (int i = 0; i < epDraws.size(); i++) {
				if (i == 0) {
					cmd.append("[outv").append(i).append("]").append("[outv").append(i + 1).append("]");
				} else {
					cmd.append("[outo").append(i - 1).append("]").append("[outv").append(i + 1).append("]");
				}
				cmd.append("overlay=").append(epDraws.get(i).getPicX()).append(":").append(epDraws.get(i).getPicY());
				if (epDraws.get(i).isAnimation()) {
					cmd.append(":shortest=1");
				}
				if (i < epDraws.size() - 1) {
					cmd.append("[outo").append(i).append("];");
				}
			}
			isFilter = true;
		} else {
			if (epVideo.getFilters() != null) {
				cmd.append(" -filter_complex ").append(epVideo.getFilters());
				isFilter = true;
			}
			//设置输出分辨率
			if (outputOption.width != 0) {
				if (epVideo.getFilters() != null) {
					cmd.append(",scale=").append(outputOption.width).append(":").append(outputOption.height)
							.append(",setdar=").append(outputOption.getSar());
				} else {
					cmd.append(" -filter_complex scale=").append(outputOption.width).append(":").append(outputOption.height)
							.append(",setdar=").append(outputOption.getSar());
					isFilter = true;
				}
			}
		}

		//输出选项
		cmd.append(outputOption.getOutputInfo());
		if (!isFilter && outputOption.getOutputInfo().isEmpty()) {
			cmd.append(" -vcodec copy -acodec copy");
		}
		cmd.append(" ").append(outputOption.outPath);
		//执行命令
		execCmd(cmd.toString(), onEditorListener);
	}

	/**
	 * 合并多个视频
	 *
	 * @param epVideos     需要合并的视频集合
	 * @param outputOption 输出选项配置
	 */
	public  static void merge(List<VideoDeal> epVideos, OutputOption outputOption, JniProgressListener onEditorListener) {
		//设置默认宽高
		outputOption.width = outputOption.width == 0 ? DEFAULT_WIDTH : outputOption.width;
		outputOption.height = outputOption.height == 0 ? DEFAULT_HEIGHT : outputOption.height;
		//判断数量
		if (epVideos.size() > 1) {
			StringBuilder cmd = new StringBuilder("-y");
			//添加输入标示
			for (VideoDeal e : epVideos) {
				if (e.getVideoClip()) {
					cmd.append(" -ss ").append(e.getClipStart()).append(" -t ").append(e.getClipDuration());
				}
				cmd.append(" -i ").append(e.getVideoPath());
			}
			for (VideoDeal e : epVideos) {
				ArrayList<VideoSpecialEffects> epDraws = e.getEpDraws();
				if (epDraws.size() > 0) {
					for (VideoSpecialEffects ep : epDraws) {
						if (ep.isAnimation()) cmd.append(" -ignore_loop 0");
						cmd.append(" -i ").append(ep.getPicPath());
					}
				}
			}
			//添加滤镜标识
			cmd.append(" -filter_complex ");
			for (int i = 0; i < epVideos.size(); i++) {
				StringBuilder filter = epVideos.get(i).getFilters() == null ? new StringBuilder("") : epVideos.get(i).getFilters().append(",");
				cmd.append("[").append(i).append(":v]").append(filter).append("scale=").append(outputOption.width).append(":").append(outputOption.height)
						.append(",setdar=").append(outputOption.getSar()).append("[outv").append(i).append("];");
			}
			//添加标记和处理宽高
			int drawNum = epVideos.size();//图标计数器
			for (int i = 0; i < epVideos.size(); i++) {
				for (int j = 0; j < epVideos.get(i).getEpDraws().size(); j++) {
					cmd.append("[").append(drawNum++).append(":0]").append(epVideos.get(i).getEpDraws().get(j).getPicFilter()).append("scale=")
							.append(epVideos.get(i).getEpDraws().get(j).getPicWidth()).append(":").append(epVideos.get(i).getEpDraws().get(j)
							.getPicHeight()).append("[p").append(i).append("a").append(j).append("];");
				}
			}
			//添加图标操作
			for (int i = 0; i < epVideos.size(); i++) {
				for (int j = 0; j < epVideos.get(i).getEpDraws().size(); j++) {
					cmd.append("[outv").append(i).append("][p").append(i).append("a").append(j).append("]overlay=")
							.append(epVideos.get(i).getEpDraws().get(j).getPicX()).append(":")
							.append(epVideos.get(i).getEpDraws().get(j).getPicY());
					if (epVideos.get(i).getEpDraws().get(j).isAnimation()) {
						cmd.append(":shortest=1");
					}
					cmd.append("[outv").append(i).append("];");
				}
			}
			//开始合成视频
			for (int i = 0; i < epVideos.size(); i++) {
				cmd.append("[outv").append(i).append("]");
			}
			cmd.append("concat=n=").append(epVideos.size()).append(":v=1:a=0[outv];");
			for (int i = 0; i < epVideos.size(); i++) {
				cmd.append("[").append(i).append(":a]");
			}
			cmd.append("concat=n=").append(epVideos.size()).append(":v=0:a=1[outa] -map [outv] -map [outa]")
					.append(outputOption.getOutputInfo()).append(" ").append(outputOption.outPath);
			//执行命令
			execCmd(cmd.toString(), onEditorListener);
		} else {
			throw new RuntimeException("Need more than one video");
		}
	}

	/**
	 * 无损合并多个视频
	 * <p>
	 * 注意：此方法要求视频格式非常严格，需要合并的视频必须分辨率相同，帧率和码率也得相同
	 *
	 * @param epVideos         需要合并的视频的集合
	 * @param outputOption     输出选项
	 * @param onEditorListener 回调监听
	 */
	public static void mergeByLc(List<VideoDeal> epVideos, OutputOption outputOption, final JniProgressListener onEditorListener) {
//		String appDir = context.getFilesDir().getAbsolutePath() + "/EpVideos/";
//		String fileName = "ffmpeg_concat.txt";
//		List<String> videos = new ArrayList<>();
//		for (VideoDeal e:epVideos) {
//			videos.add(e.getVideoPath());
//		}
//		FileUtils.writeTxtToFile(videos, appDir, fileName);
//		String cmd = "-y -f concat -safe 0 -i " + appDir + fileName + " -c copy " + outputOption.outPath;
//		execCmd(cmd, onEditorListener);
	}

	/**
	 * 添加背景音乐
	 *
	 * @param videoin          视频文件
	 * @param audioin          音频文件
	 * @param output           输出路径
	 * @param videoVolume      视频原声音音量(例:0.7为70%)
	 * @param audioVolume      背景音乐音量(例:1.5为150%)
	 * @param onEditorListener 回调监听
	 */
	public static void music(String videoin, String audioin, String output, float videoVolume, float audioVolume, JniProgressListener onEditorListener) {
		String cmd = "-y -i " + videoin + " -i " + audioin + " -filter_complex [0:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=" + videoVolume + "[a0];[1:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=" + audioVolume + "[a1];[a0][a1]amix=inputs=2:duration=first[aout] -map [aout] -ac 2 -c:v copy -map 0:v:0 " + output;
		Log.v("addText", cmd);
		execCmd(cmd, onEditorListener);
	}


	/**
	 * 输出选项设置
	 */
	public static class OutputOption {
		String outPath;//输出路径
		public int frameRate = 0;//帧率
		public int bitRate = 0;//比特率(一般设置10M)
		public String outFormat = "";//输出格式(目前暂时只支持mp4,x264,mp3,gif)
		public int width = 0;//输出宽度
		public int height = 0;//输出高度

		public OutputOption(String outPath) {
			this.outPath = outPath;
		}

		/**
		 * 获取宽高比
		 *
		 * @return
		 */
		public String getSar() {
			String res;
			float sar = (float) width / height;
			if (sar == (float) 16 / 9) {
				res = "16:9";
			} else if (sar == (float) 9 / 16) {
				res = "9:16";
			} else if (sar == (float) 4 / 3) {
				res = "4:3";
			} else if (sar == (float) 3 / 4) {
				res = "3:4";
			} else if (sar == 1.0f) {
				res = "1:1";
			} else {
				res = width + ":" + height;
			}
			return res;
		}

		/**
		 * 获取输出信息
		 *
		 * @return
		 */
		String getOutputInfo() {
			StringBuilder res = new StringBuilder();
			if (frameRate != 0) {
				res.append(" -r ").append(frameRate);
			}
			if (bitRate != 0) {
				res.append(" -b ").append(bitRate).append("M");
			}
			if (!outFormat.isEmpty()) {
				res.append(" -f ").append(outFormat);
			}
			return res.toString();
		}
	}

	/**
	 * 开始处理
	 *
	 * @param cmd 命令
	 */
	public  static void execCmd(String cmd, final JniProgressListener onEditorListener) {
		Log.v("ffmpeg", "cmd:" + cmd);
		cmd = "ffmpeg " + cmd;
		String[] cmds = cmd.split(" ");
		 FFmpegCmd.getInstance().exec(cmds,onEditorListener);
	}


	public static void stop(){
		FFmpegCmd.getInstance().stop();
    }
}
