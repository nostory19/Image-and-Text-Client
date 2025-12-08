工程训练营-图文客户端实现过程
整机架构模式：MVVM + 模块化设计
```
├── Model层（数据模型）
│   ├── Post.java - 帖子数据模型
│   └── PostDetail.java - 帖子详情数据模型
├── ViewModel层（业务逻辑）
│   ├── PostViewModel.java - 帖子列表业务逻辑
│   ├── PostDetailViewModel.java - 帖子详情业务逻辑
│   └── MineViewModel.java - 个人中心业务逻辑
├── View层（UI展示）
│   ├── MainActivity.java - 主容器Activity
│   ├── HomeFragment.java - 首页Fragment
│   ├── MineFragment.java - 我的页面Fragment
│   └── PostDetailActivity.java - 帖子详情页
├── Repository层（数据仓库）
│   └── PostRepository.java - 数据获取和管理
└── Adapter层（适配器）
    ├── PostAdapter.java - 帖子列表适配器
    └── ImagePagerAdapter.java - 图片轮播适配器
```