package me.himi.love.ui;

import java.util.ArrayList;
import java.util.List;

import me.himi.love.AppServiceImpl;
import me.himi.love.IAppService.OnLoadUserVisitorsListener;
import me.himi.love.IAppService.UserVisitorsParams;
import me.himi.love.MyApplication;
import me.himi.love.R;
import me.himi.love.adapter.UserVisitorsAdapter;
import me.himi.love.entity.VisitorUser;
import me.himi.love.ui.base.BaseActivity;
import me.himi.love.util.ActivityUtil;
import me.himi.love.util.ToastFactory;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

/**
 * @ClassName:UserVisitorsActivity
 * @author sparklee liduanwei_911@163.com
 * @date Nov 5, 2014 3:50:36 PM
 */
public class UserVisitorsActivity extends BaseActivity implements OnItemClickListener {

    com.handmark.pulltorefresh.library.PullToRefreshListView mVisitorsListView;

    me.himi.love.adapter.UserVisitorsAdapter mUserVisitorsAdapter;

    View emptyView;// 没有留言展示的view

    int targetUserId; // 需要查询的目标用户ID的访客

    @Override
    protected void onCreate(Bundle arg0) {
	super.onCreate(arg0);
	requestWindowFeature(Window.FEATURE_NO_TITLE);
	setContentView(R.layout.activity_user_visitors);

	init();

    }

    private void init() {

	targetUserId = getIntent().getIntExtra("target_user_id", 0);
	String nickname = getIntent().getStringExtra("user_nickname");
	if (targetUserId == 0) {
	    // 没有指定目标用户ID
	}
	if (isCurrentLoginedUser()) {
	    nickname = "我";
	}

	TextView tvTopTitle = (TextView) findViewById(R.id.layout_nav_top).findViewById(R.id.tv_top_title);
	TextView tvTopAction = (TextView) findViewById(R.id.layout_nav_top).findViewById(R.id.tv_top_action);
	tvTopAction.setText("");
	tvTopTitle.setText(nickname + " 的访客");
	tvTopTitle.setOnClickListener(new View.OnClickListener() {

	    @Override
	    public void onClick(View v) {
		// TODO Auto-generated method stub
		finish();
	    }
	});

	mVisitorsListView = (com.handmark.pulltorefresh.library.PullToRefreshListView) findViewById(R.id.listview_visitors);

	// 使用代理 设置ImageLoader  在 ListView 滚动时不加载图片
	mVisitorsListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));

	mUserVisitorsAdapter = new UserVisitorsAdapter(this, new ArrayList<VisitorUser>());

	mVisitorsListView.setAdapter(mUserVisitorsAdapter);

	mVisitorsListView.setMode(Mode.BOTH);

	mVisitorsListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

	    @Override
	    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		// TODO Auto-generated method stub
		pageNumber = 1;
		loadUsers();
	    }

	    @Override
	    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		// TODO Auto-generated method stub
		loadUsers();
	    }
	});

	mVisitorsListView.setOnItemClickListener(this);

	loadUsers();
    }

    int pageNumber = 1;

    private View progressBar;

    private void loadUsers() {

	if (!ActivityUtil.hasNetwork(this)) {
	    ToastFactory.getToast(this, "请开启网络").show();
	    return;
	}

	if (null == progressBar) {
	    progressBar = new ProgressBar(this);
	    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
	    params.gravity = Gravity.CENTER;
	    getWindow().addContentView(progressBar, params);
	}

	UserVisitorsParams params = new UserVisitorsParams();
	params.page = pageNumber;
	params.pageSize = 10;
	params.userId = targetUserId;

	AppServiceImpl.getInstance().loadUserVisitors(params, new OnLoadUserVisitorsListener() {

	    @Override
	    public void onSuccess(List<VisitorUser> users) {

		progressBar.setVisibility(View.GONE);
		if (users.size() != 0) {
		    if (pageNumber == 1) {
			mUserVisitorsAdapter.getList().clear();
		    }
		    mUserVisitorsAdapter.addAll(users);

		} else {

		    if (mUserVisitorsAdapter.getList().size() == 0) {
			showEmptyView();
			ActivityUtil.show(UserVisitorsActivity.this, "暂无访客");
		    } else {
			ToastFactory.getToast(UserVisitorsActivity.this, "暂无更多访客").show();
		    }

		}
		//

		pageNumber++;

		mVisitorsListView.onRefreshComplete();
	    }

	    @Override
	    public void onFailure(String errorMsg) {
		// TODO Auto-generated method stub
		ActivityUtil.show(UserVisitorsActivity.this, errorMsg);

		mVisitorsListView.onRefreshComplete();
		progressBar.setVisibility(View.GONE);
	    }
	});
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	position = position - 1;//mListView.getHeaderViewsCount();

	if (position <= -1 || position >= mUserVisitorsAdapter.getList().size()) {
	    return;
	}
	VisitorUser user = mUserVisitorsAdapter.getList().get(position);

	Intent intent = new Intent();
	intent.setClass(this, UserInfoTextActivity.class);
	intent.putExtra("user_id", user.getUserId());
	intent.putExtra("user_nickname", user.getNickname());
	intent.putExtra("user_face_url", user.getFaceUrl());
	startActivity(intent);

	// 结束掉
	finish();
    }

    /**
     * 没有访客
     */
    private void showEmptyView() {

	// 
	if (null == emptyView) {
	    emptyView = LayoutInflater.from(UserVisitorsActivity.this).inflate(R.layout.empty_visitors, null);
	}
	mVisitorsListView.setEmptyView(emptyView);
    }

    /**
     * 当前查看的访客是否属于当前登录用户
     * @return
     */
    private boolean isCurrentLoginedUser() {
	return this.targetUserId == MyApplication.getInstance().getCurrentLoginedUser().getUserId();
    }

}
