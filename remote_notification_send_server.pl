use OAuth::Lite;
use OAuth::Lite::Consumer;
use JSON;
 
sub send_remote_notification_to_single_user {
    my ($user_id, $request) = @_;
 
    my $request_url
        = "http://sb.sp.mbga-platform.jp/social/api/restful/v2/remote_notification/\@app/\@all/${user_id}";
 
    my $oauth = OAuth::Lite::Consumer->new(
        consumer_key    => '{consumer_key}',
        consumer_secret => '{consumer_secret}',
        realm           => '',
    );
 
    my $res = $oauth->request(
        method  => 'POST',
        url     => $request_url,
        content => to_json($request)
    );
}
 
send_remote_notification_to_single_user({user_id}, +{
    payload => +{
        message => "RestfulAPI テストメッセージです",
        extras  => +{
        },
    },
});
